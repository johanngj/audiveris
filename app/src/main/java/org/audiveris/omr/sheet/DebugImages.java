//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                      D e b u g I m a g e s                                     //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//
//  Copyright © Audiveris 2025. All rights reserved.
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the
//  GNU Affero General Public License as published by the Free Software Foundation, either version
//  3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
//  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//  See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this
//  program.  If not, see <http://www.gnu.org/licenses/>.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package org.audiveris.omr.sheet;

import org.audiveris.omr.glyph.Shape;
import org.audiveris.omr.sheet.grid.LineInfo;
import org.audiveris.omr.sig.SIGraph;
import org.audiveris.omr.sig.inter.Inter;
import org.audiveris.omr.step.OmrStep;
import org.audiveris.omr.util.HorizontalSide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Class <code>DebugImages</code> generates visualization images at various processing steps
 * for debugging and analysis of the OMR pipeline.
 * <p>
 * Output images show detected elements overlaid on the original image with colored bounding boxes
 * and labels.
 *
 * @author Audiveris Development
 */
public class DebugImages
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(DebugImages.class);

    /** Global flag to enable debug image generation. */
    private static boolean enabled = false;

    /** Output folder for debug images. */
    private static Path outputFolder = null;

    /** Colors for different shape categories. */
    private static final Map<String, Color> CATEGORY_COLORS = new HashMap<>();

    static {
        CATEGORY_COLORS.put("HEAD", new Color(255, 0, 0, 180));       // Red - note heads
        CATEGORY_COLORS.put("STEM", new Color(0, 255, 0, 180));       // Green - stems
        CATEGORY_COLORS.put("BEAM", new Color(0, 0, 255, 180));       // Blue - beams
        CATEGORY_COLORS.put("REST", new Color(255, 165, 0, 180));     // Orange - rests
        CATEGORY_COLORS.put("CLEF", new Color(128, 0, 128, 180));     // Purple - clefs
        CATEGORY_COLORS.put("KEY", new Color(255, 192, 203, 180));    // Pink - key signatures
        CATEGORY_COLORS.put("TIME", new Color(0, 255, 255, 180));     // Cyan - time signatures
        CATEGORY_COLORS.put("FLAG", new Color(255, 255, 0, 180));     // Yellow - flags
        CATEGORY_COLORS.put("BARLINE", new Color(139, 69, 19, 180));  // Brown - barlines
        CATEGORY_COLORS.put("SLUR", new Color(255, 20, 147, 180));    // Deep pink - slurs
        CATEGORY_COLORS.put("TEXT", new Color(64, 224, 208, 180));    // Turquoise - text
        CATEGORY_COLORS.put("OTHER", new Color(128, 128, 128, 180));  // Gray - other
    }

    //~ Constructors -------------------------------------------------------------------------------

    private DebugImages ()
    {
        // Not meant to be instantiated
    }

    //~ Methods ------------------------------------------------------------------------------------

    //---------//
    // enable  //
    //---------//
    /**
     * Enable debug image generation.
     *
     * @param folder the output folder for debug images
     */
    public static void enable (Path folder)
    {
        enabled = true;
        outputFolder = folder;
        logger.info("Debug images enabled, output folder: {}", folder);
    }

    //-----------//
    // isEnabled //
    //-----------//
    /**
     * Check if debug image generation is enabled.
     *
     * @return true if enabled
     */
    public static boolean isEnabled ()
    {
        return enabled;
    }

    //----------//
    // generate //
    //----------//
    /**
     * Generate a debug image for the given sheet at the specified step.
     *
     * @param sheet the sheet being processed
     * @param step  the current processing step
     */
    public static void generate (Sheet sheet,
                                 OmrStep step)
    {
        if (!enabled || outputFolder == null) {
            return;
        }

        try {
            // Create output folder if needed
            Files.createDirectories(outputFolder);

            // Get the base image
            BufferedImage baseImage = getBaseImage(sheet);
            if (baseImage == null) {
                logger.warn("Cannot get base image for sheet {}", sheet.getId());
                return;
            }

            // Create overlay image
            BufferedImage output = new BufferedImage(
                    baseImage.getWidth(),
                    baseImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            Graphics2D g = output.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw base image
            g.drawImage(baseImage, 0, 0, null);

            // Draw overlays based on step
            drawOverlays(g, sheet, step);

            // Draw legend
            drawLegend(g, step);

            g.dispose();

            // Save image
            String filename = String.format("%s_%02d_%s.png",
                    sheet.getStub().getBook().getRadix(),
                    sheet.getStub().getNumber(),
                    step.name().toLowerCase());
            Path outputPath = outputFolder.resolve(filename);
            ImageIO.write(output, "PNG", outputPath.toFile());
            logger.info("Debug image saved: {}", outputPath);

        } catch (IOException ex) {
            logger.warn("Failed to generate debug image for step {}: {}", step, ex.getMessage());
        }
    }

    //---------------//
    // generateFinal //
    //---------------//
    /**
     * Generate a final summary image showing all recognized symbols.
     *
     * @param sheet the sheet to visualize
     */
    public static void generateFinal (Sheet sheet)
    {
        if (!enabled || outputFolder == null) {
            return;
        }

        try {
            Files.createDirectories(outputFolder);

            BufferedImage baseImage = getBaseImage(sheet);
            if (baseImage == null) {
                return;
            }

            BufferedImage output = new BufferedImage(
                    baseImage.getWidth(),
                    baseImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            Graphics2D g = output.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(baseImage, 0, 0, null);

            // Draw all inters from all systems
            drawAllInters(g, sheet);

            // Draw staff lines
            drawStaffLines(g, sheet);

            // Draw legend
            drawFinalLegend(g, sheet);

            g.dispose();

            String filename = String.format("%s_%02d_final.png",
                    sheet.getStub().getBook().getRadix(),
                    sheet.getStub().getNumber());
            Path outputPath = outputFolder.resolve(filename);
            ImageIO.write(output, "PNG", outputPath.toFile());
            logger.info("Final debug image saved: {}", outputPath);

            // Also generate JSON report
            generateReport(sheet);

        } catch (IOException ex) {
            logger.warn("Failed to generate final debug image: {}", ex.getMessage());
        }
    }

    //--------------//
    // getBaseImage //
    //--------------//
    private static BufferedImage getBaseImage (Sheet sheet)
    {
        Picture picture = sheet.getPicture();
        if (picture == null) {
            return null;
        }

        // Try to get the gray source image
        try {
            ij.process.ByteProcessor source = picture.getSource(Picture.SourceKey.GRAY);
            if (source != null) {
                return source.getBufferedImage();
            }
        } catch (Exception ex) {
            logger.debug("Could not get GRAY source: {}", ex.getMessage());
        }

        return null;
    }

    //--------------//
    // drawOverlays //
    //--------------//
    private static void drawOverlays (Graphics2D g,
                                      Sheet sheet,
                                      OmrStep step)
    {
        switch (step) {
        case BINARY:
            // Just show the binarized image - base image is enough
            break;

        case GRID:
            drawStaffLines(g, sheet);
            break;

        case HEADS:
            drawIntersByCategory(g, sheet, "HEAD");
            break;

        case STEMS:
            drawIntersByCategory(g, sheet, "HEAD");
            drawIntersByCategory(g, sheet, "STEM");
            break;

        case BEAMS:
            drawIntersByCategory(g, sheet, "HEAD");
            drawIntersByCategory(g, sheet, "STEM");
            drawIntersByCategory(g, sheet, "BEAM");
            break;

        case SYMBOLS:
            drawAllInters(g, sheet);
            break;

        default:
            // For other steps, show current state of all inters
            drawAllInters(g, sheet);
            break;
        }
    }

    //----------------//
    // drawStaffLines //
    //----------------//
    private static void drawStaffLines (Graphics2D g,
                                        Sheet sheet)
    {
        StaffManager staffManager = sheet.getStaffManager();
        if (staffManager == null) {
            return;
        }

        g.setColor(new Color(0, 100, 255, 150));
        g.setStroke(new BasicStroke(2.0f));

        for (Staff staff : staffManager.getStaves()) {
            for (LineInfo line : staff.getLines()) {
                g.drawLine(
                        (int) line.getEndPoint(HorizontalSide.LEFT).getX(),
                        (int) line.getEndPoint(HorizontalSide.LEFT).getY(),
                        (int) line.getEndPoint(HorizontalSide.RIGHT).getX(),
                        (int) line.getEndPoint(HorizontalSide.RIGHT).getY());
            }
        }
    }

    //----------------------//
    // drawIntersByCategory //
    //----------------------//
    private static void drawIntersByCategory (Graphics2D g,
                                              Sheet sheet,
                                              String category)
    {
        Color color = CATEGORY_COLORS.getOrDefault(category, CATEGORY_COLORS.get("OTHER"));
        g.setColor(color);
        g.setStroke(new BasicStroke(2.0f));
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));

        for (SystemInfo system : sheet.getSystems()) {
            SIGraph sig = system.getSig();
            if (sig == null) {
                continue;
            }

            for (Inter inter : sig.vertexSet()) {
                if (matchesCategory(inter.getShape(), category)) {
                    drawInter(g, inter, color);
                }
            }
        }
    }

    //---------------//
    // drawAllInters //
    //---------------//
    private static void drawAllInters (Graphics2D g,
                                       Sheet sheet)
    {
        g.setStroke(new BasicStroke(2.0f));
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));

        for (SystemInfo system : sheet.getSystems()) {
            SIGraph sig = system.getSig();
            if (sig == null) {
                continue;
            }

            for (Inter inter : sig.vertexSet()) {
                String category = getCategory(inter.getShape());
                Color color = CATEGORY_COLORS.getOrDefault(category, CATEGORY_COLORS.get("OTHER"));
                drawInter(g, inter, color);
            }
        }
    }

    //-----------//
    // drawInter //
    //-----------//
    private static void drawInter (Graphics2D g,
                                   Inter inter,
                                   Color color)
    {
        Rectangle bounds = inter.getBounds();
        if (bounds == null) {
            return;
        }

        g.setColor(color);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Draw shape name and grade
        String label = String.format("%s %.2f", 
                getShortName(inter.getShape()), 
                inter.getGrade());
        
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(bounds.x, bounds.y - 12, label.length() * 6, 12);
        g.setColor(Color.WHITE);
        g.drawString(label, bounds.x + 2, bounds.y - 2);
    }

    //------------//
    // drawLegend //
    //------------//
    private static void drawLegend (Graphics2D g,
                                    OmrStep step)
    {
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(10, 10, 200, 30);
        g.setColor(Color.WHITE);
        g.drawString("Step: " + step.name(), 20, 30);
    }

    //-----------------//
    // drawFinalLegend //
    //-----------------//
    private static void drawFinalLegend (Graphics2D g,
                                         Sheet sheet)
    {
        int y = 10;
        int x = 10;
        
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        // Background
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(x, y, 180, CATEGORY_COLORS.size() * 18 + 30);
        
        g.setColor(Color.BLACK);
        g.drawString("Legend:", x + 5, y + 15);
        y += 25;
        
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (Map.Entry<String, Color> entry : CATEGORY_COLORS.entrySet()) {
            g.setColor(entry.getValue());
            g.fillRect(x + 5, y - 10, 15, 15);
            g.setColor(Color.BLACK);
            g.drawString(entry.getKey(), x + 25, y);
            y += 18;
        }
    }

    //-----------------//
    // matchesCategory //
    //-----------------//
    private static boolean matchesCategory (Shape shape,
                                            String category)
    {
        if (shape == null) {
            return false;
        }

        String shapeName = shape.name().toUpperCase();
        return switch (category) {
            case "HEAD" -> shapeName.contains("HEAD") || shapeName.contains("NOTEHEAD");
            case "STEM" -> shapeName.contains("STEM");
            case "BEAM" -> shapeName.contains("BEAM");
            case "REST" -> shapeName.contains("REST");
            case "CLEF" -> shapeName.contains("CLEF");
            case "KEY" -> shapeName.contains("KEY") || shapeName.contains("SHARP") || 
                          shapeName.contains("FLAT") || shapeName.contains("NATURAL");
            case "TIME" -> shapeName.contains("TIME") || shapeName.startsWith("COMMON_") ||
                          shapeName.startsWith("CUT_");
            case "FLAG" -> shapeName.contains("FLAG");
            case "BARLINE" -> shapeName.contains("BARLINE");
            case "SLUR" -> shapeName.contains("SLUR") || shapeName.contains("TIE");
            case "TEXT" -> shapeName.contains("TEXT") || shapeName.contains("LYRIC") ||
                          shapeName.contains("DYNAMICS");
            default -> false;
        };
    }

    //-------------//
    // getCategory //
    //-------------//
    private static String getCategory (Shape shape)
    {
        if (shape == null) {
            return "OTHER";
        }

        for (String category : CATEGORY_COLORS.keySet()) {
            if (matchesCategory(shape, category)) {
                return category;
            }
        }
        return "OTHER";
    }

    //--------------//
    // getShortName //
    //--------------//
    private static String getShortName (Shape shape)
    {
        if (shape == null) {
            return "?";
        }
        String name = shape.name();
        // Truncate long names
        return name.length() > 12 ? name.substring(0, 12) : name;
    }

    //----------------//
    // generateReport //
    //----------------//
    private static void generateReport (Sheet sheet)
    {
        try {
            Map<String, Integer> counts = new HashMap<>();
            Map<String, Double> avgGrades = new HashMap<>();
            Map<String, Integer> gradeCounts = new HashMap<>();

            for (SystemInfo system : sheet.getSystems()) {
                SIGraph sig = system.getSig();
                if (sig == null) {
                    continue;
                }

                for (Inter inter : sig.vertexSet()) {
                    String category = getCategory(inter.getShape());
                    counts.merge(category, 1, Integer::sum);
                    avgGrades.merge(category, inter.getGrade(), Double::sum);
                    gradeCounts.merge(category, 1, Integer::sum);
                }
            }

            // Calculate averages
            for (String category : avgGrades.keySet()) {
                avgGrades.put(category, avgGrades.get(category) / gradeCounts.get(category));
            }

            // Write JSON report
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"sheet\": ").append(sheet.getStub().getNumber()).append(",\n");
            json.append("  \"systems\": ").append(sheet.getSystems().size()).append(",\n");
            json.append("  \"categories\": {\n");

            int i = 0;
            for (String category : counts.keySet()) {
                json.append("    \"").append(category).append("\": {\n");
                json.append("      \"count\": ").append(counts.get(category)).append(",\n");
                json.append("      \"avgGrade\": ").append(String.format("%.3f", avgGrades.get(category))).append("\n");
                json.append("    }");
                if (++i < counts.size()) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("  }\n");
            json.append("}\n");

            String filename = String.format("%s_%02d_report.json",
                    sheet.getStub().getBook().getRadix(),
                    sheet.getStub().getNumber());
            Path reportPath = outputFolder.resolve(filename);
            Files.writeString(reportPath, json.toString());
            logger.info("Debug report saved: {}", reportPath);

        } catch (IOException ex) {
            logger.warn("Failed to generate report: {}", ex.getMessage());
        }
    }
}
