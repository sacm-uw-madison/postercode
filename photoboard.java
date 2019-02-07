import java.io.*;
import java.util.*;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;//PdfWriter;
import java.awt.Color;

public class photoboard {
	// The final board will be resized to this height:
	private static final float PREFERREDHEIGHT = 40.f;

	private static final int ROWSIZE = 20;
	private static final float IMAGEWIDTH = 1.9f * 72;
	private static final float IMAGEMARGIN = .1f * 72;
	private static final float PHOTOWIDTH = (IMAGEWIDTH - IMAGEMARGIN);
//	private static final float LABELRECTHEIGHT = .625f * 72;
	private static final float LABELRECTHEIGHT = .625f * 48;

	private static BaseFont font, titleFont;

	private static Image unknownPhoto;

	static {
		try {
			font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252,
					BaseFont.NOT_EMBEDDED);
			titleFont = BaseFont.createFont("neuropoli.ttf", BaseFont.CP1252,
					BaseFont.NOT_EMBEDDED);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Person[] people;
		String inputFile = "people.dat", outputFile = "poster.pdf";
		if (args.length > 0)
			inputFile = args[0];
		if (args.length > 1)
			outputFile = args[1];

		people = Person
				.openFiles(inputFile, "phone.dat", "preferred_names.dat");

		takeRollCall(people);
		makePoster(people, outputFile);
	}

	public static void takeRollCall(Person[] people) {
		int i;
		System.out.println("Missing photo:");
		for (i = 0; i < people.length; i++) {
			if (!people[i].hasPhoto())
				System.out.println(people[i].getName() + "  <" + people[i].getID() + "@cs.wisc.edu>");
		}
	}

	public static void makePoster(Person[] people, String outputFile) {
		int i;
		int numRows;
		float x, y;
		float width, height;
		Document document;
		PdfWriter writer;
		PdfContentByte contentByte;
		System.err.println("Creating photoboard poster...");

		numRows = (people.length + ROWSIZE - 1) / ROWSIZE;

		width = ROWSIZE * IMAGEWIDTH / 72 + 2;
		height = numRows * 3 + 2 + 1 + 0.5f;
		System.err.println("\tposter is " + width + "\" x " + height + "\"");
		System.err.println("\t" + people.length + " people in " + numRows
				+ " rows");
		// make the document object
		document = new Document(new Rectangle(width * 72, height * 72), 0, 0,
				0, 0);

		try {
			// get a writer to the file
			writer = PdfWriter.getInstance(document, new FileOutputStream(
					outputFile));

			// make the sucker
			document.open();
			unknownPhoto = getImage("unknownphoto.png");
			contentByte = writer.getDirectContent();
			stampBackground(document, "bamboo_mat_texture.jpg");
			//stampTitle1(contentByte, width/2*72, (height-1.5f)*72);
			stampTitle2(contentByte, width / 2 * 72, (height - 2.2f) * 72);
			// stampTitle(document, "title.gif");
			for (i = 0; i < people.length; i++) {
				x = (i % ROWSIZE) * IMAGEWIDTH + 72f;
				y = ((numRows - 1 - (i / ROWSIZE)) * 3 + 1) * 72f;
				stampPerson(contentByte, people[i], x, y);
			}

			writer.setPageSize(new Rectangle((PREFERREDHEIGHT * width / height) * 72.f, PREFERREDHEIGHT * 72.f));
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

	public static void stampTitle2(PdfContentByte cb, float x, float y) {
		try {
			Image image = getImage("title.png");

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

	public static void stampTitle1(PdfContentByte cb, float x, float y) {
		// the writing
		cb.beginText();
		cb.setRGBColorFill(0x80, 0x80, 0x80);
		cb.setFontAndSize(titleFont, 96);
		cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
				"Computer Science Graduate Student - 2017", x, y, 0);
		cb.endText();

	}

	public static void stampPerson(PdfContentByte cb, Person person, float x,
			float y) {
		Image image;

		// the white rectangle
		cb.roundRectangle(x, y + 43, PHOTOWIDTH, LABELRECTHEIGHT, 10);
		cb.setRGBColorFill(0xff, 0xff, 0xff);
		cb.setRGBColorStroke(0, 0, 0);
		cb.fill();

		float centerpt = PHOTOWIDTH / 2.f;

		// the writing
		cb.beginText();
		cb.setRGBColorFill(0, 0, 0);
		stampString(cb, person.getName(), 12f, x + centerpt, y + 30 + 30);
		stampString(cb, person.getID() + "@cs.wisc.edu", 10f, x + centerpt,
				y + 18 + 30);
		stampString(cb, person.getOfficeString(), 10f, x + centerpt, y + 6 + 30);
		cb.endText();

		// the picture
		////cb.setColorFill(new Color(0.f, 0.f, 0.f, 0.f));
		cb.setColorFill(new BaseColor(0.f, 0.f, 0.f, 0.f));
		////cb.setColorStroke(new Color(1.f, 1.f, 1.f));
		cb.setColorStroke(new BaseColor(1.f, 1.f, 1.f));
		cb.rectangle(x - 1, y + 3*72 - PHOTOWIDTH - 1, PHOTOWIDTH + 2,
				PHOTOWIDTH + 2);
		cb.fill();

		try {
			image = grabImage(person);
			if (image == null)
				image = unknownPhoto;

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

	private static void stampString(PdfContentByte cb, String s,
			float fontSize, float x, float y) {
		while (font.getWidthPoint(s, fontSize) >= PHOTOWIDTH - 7.2f)
			fontSize -= 0.1;
		cb.setFontAndSize(font, fontSize);
		cb.showTextAligned(PdfContentByte.ALIGN_CENTER, s, x, y, 0);
	}

	public static void stampTitle(Document doc, String imageName) {
		Image image;
		float x, y;

		image = getImage(imageName);
		if (image == null) {
			System.err.println("couldn't find file " + imageName);
			return;
		}

		x = (doc.left() + doc.right() / 2);
		y = doc.top() - 72 - 72;

		////System.err.println(image.plainWidth() + "\t" + image.plainHeight());
		System.err.println(image.getPlainWidth() + "\t" + image.getPlainHeight());
		////image.scalePercent(95 * 72 / image.plainHeight());
		image.scalePercent(95 * 72 / image.getPlainHeight());
		////System.err.println(image.plainWidth() + "\t" + image.plainHeight());
		System.err.println(image.getPlainWidth() + "\t" + image.getPlainHeight());
		////System.err.println("\tat " + x + "\t" + (x - image.scaledWidth() / 2)
		////		+ "\t" + y);
		System.err.println("\tat " + x + "\t" + (x - image.getScaledWidth() / 2)
				+ "\t" + y);
		////image.setAbsolutePosition(x - image.scaledWidth() / 2, y);
		image.setAbsolutePosition(x - image.getScaledWidth() / 2, y);

		try {
			doc.add(image);
		}

		catch (DocumentException de) {
			System.err.println("couldn't print background");
			return;
		}
	}

	public static void stampBackground(Document doc, String imageName) {
		Image image;
		float h, w;
		float x, y;
		float minX, minY, maxX, maxY;

		image = getImage(imageName);
		if (image == null) {
			System.err.println("couldn't find file " + imageName);
			return;
		}

		////w = image.plainWidth();
		////h = image.plainHeight();
		w = image.getPlainWidth();
		h = image.getPlainHeight();
		minX = doc.left();
		maxX = doc.right();
		minY = doc.bottom();
		maxY = doc.top();

		try {
			for (x = minX; x < maxX; x += w) {
				for (y = minY; y < maxY; y += h) {
					image.setAbsolutePosition(x, y);
					doc.add(image);
				}
			}
		} catch (DocumentException de) {
			System.err.println("couldn't print background");
			return;
		}
	}

	private static Image grabImage(Person person) {
		Image image;
		image = getImage("pics/" + person.getID() + ".jpg");
		return image;
	}

	public static Image getImage(String fileName) {
		try {
			return Image.getInstance(fileName);
		} catch (Exception e) {
			return null;
		}
	}

}

class Person implements Comparable {
	String email, lastName, firstName, preferredName;

	String office, phoneNumber;

	public Person(String s, Hashtable phoneHash, Hashtable preferredNameHash) {
		String[] tokens;

		tokens = s.split(":");
		email = tokens[0];
		if (tokens.length > 1)
			lastName = tokens[1];
		if (tokens.length > 2)
			firstName = tokens[2];
		if (tokens.length > 3)
			office = tokens[3];
		if (office != null && office.length() == 0)
			office = null;

		// get preferred name & office's phone # from the hashes
		if (phoneHash != null && office != null) {
			phoneNumber = (String) phoneHash.get(office);




			if (phoneNumber != null && phoneNumber.length() == 0)
				phoneNumber = null;
		}
		if (preferredNameHash != null && email != null) {
			preferredName = (String) preferredNameHash.get(email);
			if (preferredName != null && preferredName.length() == 0)
				preferredName = null;
		}

		// System.out.println(this);
		// System.exit(0);
	}

	public boolean hasPhoto() {
		if (new File("pics/" + email + ".jpg").exists())
			return true;
		return false;
	}

	public static Person[] openFiles(String personFile, String phoneFile,
			String preferredNameFile) {
		Hashtable phoneHash, preferredNameHash;

		phoneHash = makeHashFromFile(phoneFile);
		preferredNameHash = makeHashFromFile(preferredNameFile);
		return openFile(personFile, phoneHash, preferredNameHash);
	}

	private static Hashtable makeHashFromFile(String filename) {
		BufferedReader reader;
		Hashtable hash;
		String line;
		String[] tokens;
		int i;

		if (filename == null || filename.length() == 0)
			return null;
		hash = new Hashtable();
		try {
			reader = new BufferedReader(new FileReader(filename));
			while ((line = reader.readLine()) != null) {
				i = line.indexOf("#");
				if (i >= 0)
					line = line.substring(0, i);
				if (line.length() == 0)
					continue;
				tokens = line.split(":");
				if (tokens.length >= 2)
					hash.put(tokens[0], tokens[1]);
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("WARNING--couldn't find " + filename);
			return null;
		}

		return hash;
	}

	// open up a file of people & turn it into an array of them
	public static Person[] openFile(String fileName, Hashtable phoneHash,
			Hashtable preferredNameHash) {
		BufferedReader reader;
		String line;
		Person p;
		Vector v = new Vector();
		Person[] people;

		try {
			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {
				p = new Person(line, phoneHash, preferredNameHash);
				v.add(p);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		people = (Person[]) v.toArray(new Person[v.size()]);

		for (int i = 0; i < people.length; i++)
			System.out.println(people[i]);

		Arrays.sort(people);
		return people;
	}

	public String getID() {
		return email;
	}

	public String getName() {
		if (preferredName != null && preferredName.length() > 0)
			return preferredName;
		return firstName + " " + lastName;
	}

	public String getOfficeString() {
		if (office == null || office.length() == 0)
			return "";
		if (phoneNumber == null || phoneNumber.length() == 0)
			return office;
		return office + " (" + phoneNumber + ")";
	}

	public int compareTo(Object object) {
		int result;
		Person other = (Person) object;
		result = firstName.compareTo(other.firstName);
		if (result == 0)
			result = lastName.compareTo(other.lastName);
		return result;
	}

	public String toString() {
		String name;
		if (preferredName == null)
			name = firstName + " " + lastName;
		else
			name = preferredName;
		return name + " (" + email + ") [" + office + " " + phoneNumber + "]";
	}
}
