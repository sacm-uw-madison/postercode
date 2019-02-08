
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.awt.Color;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.BaseFont;

public class Photoboard {
    ///////////////////////////////////////////////////////////////////////////
    // Some settings
    ///////////////////////////////////////////////////////////////////////////

    // The final board will be resized to this height:
    private static final float PREFERREDHEIGHT = 40.f;

    private static final int ROWSIZE = 20;
    private static final float IMAGEWIDTH = 1.9f * 72;
    private static final float IMAGEMARGIN = .1f * 72;
    private static final float PHOTOWIDTH = (IMAGEWIDTH - IMAGEMARGIN);
    // private static final float LABELRECTHEIGHT = .625f * 72;
    private static final float LABELRECTHEIGHT = .625f * 48;

    // Locations of some important files.
    public static final String UNKNOWN_PHOTO_IMAGE = "assets/unknownphoto.png";
    private static final String FONT_TTF = "assets/neuropoli.ttf";
    private static final String BACKGROUND_TEXTURE = "assets/bamboo_mat_texture.jpg";
    private static final String TITLE_IMAGE = "assets/title.png";

    public static String PICS_DIRECTORY = "pics/";

    ///////////////////////////////////////////////////////////////////////////
    // Code to build the poster
    ///////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        // Some checking.
        if (args.length < 2) {
            System.out.println("Usage: ./photoboard people.dat poster.pdf [pics/]");
            return;
        }

        // Contains people's names and usernames.
        String inputFile = args[0];
        // The PDF file with the poster.
        String outputFile = args[1];

        // Optionally, a pics/ folder
        if (args.length == 3) {
            PICS_DIRECTORY = args[2];
        }

        // Open the file if it exists.
        File peopledat = new File(inputFile);
        if (!peopledat.exists()) {
            System.out.println("File " + peopledat + " does not exist.");
        }

        // Read the file.
        List<Person> people = Person.openFile(peopledat);

        // Check for missing photos.
        takeRollCall(people);

        // Actually build the poster.
        makePoster(people, outputFile);
    }

    ///////////////////////////////////////////////////////////////////////////
    // A bunch of helper functions.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Print a message for each person missing a photo.
     */
    private static void takeRollCall(List<Person> people) {
        System.out.println("The following people are missing photos:");
        for (Person p : people) {
            if (!p.hasPhoto()) {
                System.out.println(p.getName() + " (" + p.getId() + ")");
            }
        }
    }

    /**
     * Build the poster itself.
     */
    private static void makePoster(List<Person> people, String outputFile) {
        System.out.println("Creating photoboard poster...");

        // The number of rows of photos on the poster.
        int numRows = (people.size() + ROWSIZE - 1) / ROWSIZE;

        // Size of the image.
        float width = ROWSIZE * IMAGEWIDTH / 72 + 2;
        float height = numRows * 3 + 2 + 1 + 0.5f;

        System.out.println("\tposter is " + width + "\" x " + height + "\"");
        System.out.println("\t" + people.size() + " people in " + numRows + " rows");

        Document document =
            new Document(new Rectangle(width * 72, height * 72), 0, 0, 0, 0);

        try {
            // Open some resources.
            Image unknownPhoto = getImage(UNKNOWN_PHOTO_IMAGE);
            BaseFont font =
                BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont titleFont =
                BaseFont.createFont(FONT_TTF, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // Get a writer to the file.
            PdfWriter writer = PdfWriter.getInstance(
                    document, new FileOutputStream(outputFile));
            document.open();
            PdfContentByte contentByte = writer.getDirectContent();

            // Create the background.
            stampBackground(document, BACKGROUND_TEXTURE);

            // Create the title.
            stampTitle(contentByte, width / 2 * 72, (height - 2.2f) * 72);

            // Add all the people.
            for (int i = 0; i < people.size(); i++) {
                float x = (i % ROWSIZE) * IMAGEWIDTH + 72f;
                float y = ((numRows - 1 - (i / ROWSIZE)) * 3 + 1) * 72f;
                stampPerson(contentByte, people.get(i), font, x, y);
            }

            // Set the page size.
            writer.setPageSize(new Rectangle(
                        (PREFERREDHEIGHT * width / height) * 72.f,
                        PREFERREDHEIGHT * 72.f));
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } finally {
            // close the document
            document.close();
            System.out.println("Finished making poster.");
        }
    }

    /**
     * Return the given image or `null` if unable to open it.
     */
    private static Image getImage(String fileName) {
        try {
            return Image.getInstance(fileName);
        } catch (Exception e) {
            System.out.println("Unable to load image: " + fileName + ": " + e);
            return null;
        }
    }

    /**
     * Add the background texture to the poster.
     */
    public static void stampBackground(Document doc, String imageName) {
        // Open the image. We are going to tile this image on the poster.
        Image image = getImage(imageName);
        if (image == null) {
            System.out.println("couldn't find background file " + imageName);
            return;
        }

        // Dimentions of the poster.
        final float minX = doc.left();
        final float maxX = doc.right();
        final float minY = doc.bottom();
        final float maxY = doc.top();
        final float w = image.getPlainWidth();
        final float h = image.getPlainHeight();

        try {
            for (float x = minX; x < maxX; x += w) {
                for (float y = minY; y < maxY; y += h) {
                    image.setAbsolutePosition(x, y);
                    doc.add(image);
                }
            }
        } catch (DocumentException de) {
            System.out.println("couldn't print background");
        }
    }

    /**
     * Add the title to the given document.
     */
    private static void stampTitle(PdfContentByte cb, float x, float y) {
        try {
            Image image = getImage(TITLE_IMAGE);

            if (image != null) {
                float width = ROWSIZE * IMAGEWIDTH + 2 * 72f;

                image.scaleToFit(width, IMAGEWIDTH);

                image.setAbsolutePosition(x - image.getPlainWidth() / 2, y);

                cb.addImage(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Print the given string at the given location on the poster.
     */
    private static void stampString( PdfContentByte cb, String s,
            BaseFont font, float fontSize, float x, float y)
    {
        // Shrink to fit.
        while (font.getWidthPoint(s, fontSize) >= PHOTOWIDTH - 7.2f) {
            fontSize -= 0.1;
        }

        // Set font.
        cb.setFontAndSize(font, fontSize);

        // Print.
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, s, x, y, 0);
    }

    /**
     * Add the given person to the poster at the given location.
     */
    public static void stampPerson(PdfContentByte cb, Person person,
            BaseFont font, float x, float y)
    {
        // the white rectangle
        cb.roundRectangle(x, y + 43, PHOTOWIDTH, LABELRECTHEIGHT, 10);
        cb.setRGBColorFill(0xff, 0xff, 0xff);
        cb.setRGBColorStroke(0, 0, 0);
        cb.fill();

        float centerpt = PHOTOWIDTH / 2.f;

        // the writing
        cb.beginText();
        cb.setRGBColorFill(0, 0, 0);
        stampString(cb, person.getName(), font, 12f, x + centerpt, y + 30 + 30);
        stampString(cb, person.getId() + "@cs.wisc.edu", font, 10f,
                x + centerpt, y + 18 + 30); // TODO: do we want this?
        stampString(cb, person.getOffice(), font, 10f, x + centerpt, y + 6 + 30); // TODO: do we want this?
        cb.endText();

        // the picture
        cb.setColorFill(new BaseColor(0.f, 0.f, 0.f, 0.f));
        //cb.setColorFill(new Color(0.f, 0.f, 0.f, 0.f));
        //cb.setColorStroke(new Color(1.f, 1.f, 1.f));
        cb.setColorStroke(new BaseColor(1.f, 1.f, 1.f));
        cb.rectangle(x - 1, y + 3*72 - PHOTOWIDTH - 1, PHOTOWIDTH + 2, PHOTOWIDTH + 2);
        cb.fill();

        try {
            Image image = person.getImage();
            image.setAbsolutePosition(x, y + 3*72 - PHOTOWIDTH);
            image.scaleAbsolute(PHOTOWIDTH, PHOTOWIDTH);
            cb.addImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
         * cb.setFontAndSize(font, fontSize);
         * cb.showTextAligned(PdfContentByte.ALIGN_CENTER, person.getName(),
         * x+centerpt, y+30, 0); cb.setFontAndSize(font, 10);
         * cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
         * person.getID()+"@cs.wisc.edu", x+centerpt, y+18, 0);
         * cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
         * person.getOfficeString(), x+centerpt, y+6, 0);
         */

    }

    // Some older code:

    // Use as:
    //  stampTitle(document, "title.gif");
    //
    //public static void stampTitle(Document doc, String imageName) {
    //    Image image;
    //    float x, y;

    //    image = getImage(imageName);
    //    if (image == null) {
    //        System.err.println("couldn't find file " + imageName);
    //        return;
    //    }

    //    x = (doc.left() + doc.right() / 2);
    //    y = doc.top() - 72 - 72;

    //    ////System.err.println(image.plainWidth() + "\t" + image.plainHeight());
    //    System.err.println(image.getPlainWidth() + "\t" + image.getPlainHeight());
    //    ////image.scalePercent(95 * 72 / image.plainHeight());
    //    image.scalePercent(95 * 72 / image.getPlainHeight());
    //    ////System.err.println(image.plainWidth() + "\t" + image.plainHeight());
    //    System.err.println(image.getPlainWidth() + "\t" + image.getPlainHeight());
    //    ////System.err.println("\tat " + x + "\t" + (x - image.scaledWidth() / 2)
    //    ////        + "\t" + y);
    //    System.err.println("\tat " + x + "\t" + (x - image.getScaledWidth() / 2)
    //            + "\t" + y);
    //    ////image.setAbsolutePosition(x - image.scaledWidth() / 2, y);
    //    image.setAbsolutePosition(x - image.getScaledWidth() / 2, y);

    //    try {
    //        doc.add(image);
    //    }

    //    catch (DocumentException de) {
    //        System.err.println("couldn't print background");
    //        return;
    //    }
    //}

    // Use as:
    //   stampTitle1(contentByte, width/2*72, (height-1.5f)*72);
    //
    //public static void stampTitle1(PdfContentByte cb, float x, float y) {
    //    // the writing
    //    cb.beginText();
    //    cb.setRGBColorFill(0x80, 0x80, 0x80);
    //    cb.setFontAndSize(titleFont, 96);
    //    cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
    //            "Computer Science Graduate Student - 2017", x, y, 0);
    //    cb.endText();
    //}
}

/**
 * Contains info about a single person.
 */
class Person implements Comparable<Person> {
    // Info about the person.
    private String username;
    private String lastName;
    private String firstName;
    private String office;

    /**
     * Read the given file, parsing each line into a Person. Then, return an
     * array of all people.
     */
    public static List<Person> openFile(File inputFile) {
        BufferedReader reader;

        try {
            // Open the file
            reader = new BufferedReader(new FileReader(inputFile));

            // Read each line and parse.
            List<Person> people = reader
                .lines()
                .map(line -> new Person(line))
                .collect(java.util.stream.Collectors.toList());

            // Sort
            Collections.sort(people);

            reader.close();

            return people;
        } catch (IOException e) {
            System.out.println("I/O error while parsing people: " + e);
            return new ArrayList<>();
        }
    }

    /**
     * Parses a string into a Person.
     *
     * The input string should be of the form `username:Last:First:office`.
     */
    public Person(String s) {
        // Split by colons. Check that there are 4 parts.
        String[] tokens = s.split(":");

        username = tokens[0];
        lastName = tokens[1];
        firstName = tokens[2];
        if (tokens.length >= 4) {
            office = tokens[3];
        } else {
            office = "";
        }
    }

    /**
     * Returns true if `pics/<username>.jpg` exists.
     */
    public boolean hasPhoto() {
        return new File(Photoboard.PICS_DIRECTORY + "/" + username + ".jpg").exists();
    }

    /**
     * Return a unique identifier for this person.
     */
    public String getId() {
        return username;
    }

    /**
     * Return the full name of the person.
     */
    public String getName() {
        return firstName + " " + lastName;
    }

    /**
     * Return the office number of the person.
     */
    public String getOffice() {
        return office;
    }

    /**
     * Returns this Person's image from the `pics/` directory, if there is one,
     * or the placeholder photo otherwise.
     */
    public Image getImage() {
        try {
            return Image.getInstance(Photoboard.PICS_DIRECTORY + "/" + getId() + ".jpg");
        } catch (Exception e) {
            try {
                return Image.getInstance(Photoboard.UNKNOWN_PHOTO_IMAGE);
            } catch (Exception e2) {
                System.out.println("Unable to load placeholder image: "
                        + Photoboard.UNKNOWN_PHOTO_IMAGE + " : " + e2);
                return null;
            }
        }
    }

    /**
     * Compare two students (by name, rather than number of papers).
     */
    public int compareTo(Person other) {
        int result = firstName.compareTo(other.firstName);
        if (result == 0)
            result = lastName.compareTo(other.lastName);
        return result;
    }

    // Debugging
    public String toString() {
        return getId() + " " + getName() + " " + getOffice();
    }
}
