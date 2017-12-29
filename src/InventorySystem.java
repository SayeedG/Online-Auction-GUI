// Author: Sayeed Gulmahamad

/* I wrote the code below with the assistance from the following urls:
* http://www.javacoffeebreak.com/faq/faq0034.html
* http://www.tutorialspoint.com/java/java_hashtable_class.htm
* http://javarevisited.blogspot.com/2012/01/java-hashtable-example-tutorial-code.html
*	https://www.cs.cmu.edu/~adamchik/15-121/lectures/Hashing/hashing.html
*/

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class InventorySystem {

	private static final String DELIMITER = "|";
	// This hash table will store item title.
	private Hashtable<String, String> titleStore = new Hashtable<>();
	// This hash table will store quantity.
	private Hashtable<String, String> quantityStorage = new Hashtable<>();
	// This hash table will store price.
	private Hashtable<String, String> priceStorage = new Hashtable<>();
	// This hash table will store date of auction.
	private Hashtable<String, String> auctionDateStorage = new Hashtable<>();
	// This hash table will store place.
	private Hashtable<String, String> placeStore = new Hashtable<>();
	// This hash table will store image.
		private Hashtable<String, String> imageStore = new Hashtable<>();
	private String [] ids = null;

	private Logger log = null;
	private String userId = null;
	private String logFile = null;
	private String outputFile = null;

	public InventorySystem(String userId, String logFile) {
		this.userId = userId;
		this.logFile = logFile;
		log = getLogger(logFile);
		outputFile = "output.txt";
	}

	// check is the user is the admin
	public boolean isAdmin() {
		return userId.equalsIgnoreCase(Constant.ADMIN);
	}

	// read input.txt
	public void readLine(String fileName) {
		try {
			// This will reference one line at a time
			String line = null;

			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(fileName);

			// wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
				storeData(line);
			}

			// close files.
			bufferedReader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void storeData(String line) {
		// spilt the data into array by delimiter |
		String[] dataArray = line.split("\\|");
		System.out.println(dataArray.length);
		String inventoryNo = dataArray[0];
		String description = dataArray[1];
		String quantity = dataArray[2];
		String price = dataArray[3];
		String purchaseDate = dataArray[4];
		String timestamp = dataArray[5];
		saveOrUpdate(inventoryNo, description, quantity, price, purchaseDate, timestamp, true);

	}

	//https://www.javatpoint.com/StringBuffer-class
	//https://docs.oracle.com/javase/7/docs/api/java/lang/StringBuffer.html
	//https://fresh2refresh.com/java-tutorial/java-string-buffer/
	//https://www.javatpoint.com/java-string-trim
	public void saveOrUpdate(String inventoryNo, String description, String quantity, String price, String purchaseDate,
			String timestamp, boolean isInsert) {
		StringBuffer sb = new StringBuffer();
		if (isInsert) {
			sb.append("INSERT data - ");
		} else {
			sb.append("UPDATE data - ");
		}
		sb.append("Inventory No: " + inventoryNo);
		// inventoryNo will be used as key to store the data.
		if (description != null && !description.isEmpty()) {
			// store description
			titleStore.put(inventoryNo, description.trim());
			sb.append(" , description : " + description);
		}
		if (quantity != null && !quantity.isEmpty()) {
			// store quantity
			quantityStorage.put(inventoryNo, quantity.trim());
			sb.append(" , quantity : " + quantity);
		}
		if (price != null && !price.isEmpty()) {
			// store price
			priceStorage.put(inventoryNo, price.trim());
			sb.append(" , price : " + price);
		}
		if (purchaseDate != null && !purchaseDate.isEmpty()) {
			// store purchaseDate
			auctionDateStorage.put(inventoryNo, purchaseDate.trim());
			sb.append(" , purchaseDate : " + purchaseDate);
		}
		if (timestamp != null && !timestamp.isEmpty()) {
			// store timestamp
			placeStore.put(inventoryNo, timestamp.trim());
			sb.append(" , timestamp : " + timestamp);
		}
		log.info(sb.toString());
		createDataDump(outputFile);
	}

	//delete function
	public void delete(String inventoryNo) {
		titleStore.remove(inventoryNo);
		quantityStorage.remove(inventoryNo);
		priceStorage.remove(inventoryNo);
		auctionDateStorage.remove(inventoryNo);
		placeStore.remove(inventoryNo);
		log.info("DELETE data for ID : " + inventoryNo);
		createDataDump(outputFile);
	}

	public Object [][] search(String item) throws Exception {
		System.out.println(item);
		// Create header
		String content = "ID | Item Title | Quantity | Price | Date of Auction | Place \n";

		String res;
		try {
			//create URL and find result
			res = WebpageReaderWithAgent.readHtml("https://www.shopgoodwill.com/Listings?st="+item + "&sg=&c=&s=&lp=0&hp=999999&sbn=false&spo=false&snpo=false&socs=false&sd=false&sca=false&caed=" + getTodayDate() + "&cadb=7&scs=false&sis=false&col=1&p=1&ps=40&desc=false&ss=0&UseBuyerPrefs=true");
			res = res.substring(res.indexOf("class=\"product\"") + 1);
			extractID(res);
			if(ids == null || ids.length == 0) {
				throw new RuntimeException( "No item avalable for " + item);
			}
			extractTitle(res);
			extractPrice(res);
			extractImage(res);
		} catch (Exception e) {
			throw e;
		}


		Object [][] objArray = new Object[ids.length][];

		if (ids.length > 0) {
			// iterate a keySet and get the data from all storages for the same key.
			for (int j=0; j < ids.length; j++) {
				String key = ids[j];
				Icon icon = null;
				// create content as per header.
				content = content + key + " " + DELIMITER + " " + titleStore.get(key) + " " + DELIMITER + " "
						+ priceStorage.get(key) + "\n";
				 String path = imageStore.get(key);
                 System.out.println("Get Image from " + path);
                 if(path != null) {
                 URL url = new URL(path);
                 BufferedImage image = ImageIO.read(url);
                 System.out.println("Load image into frame...");
                 icon = new ImageIcon(image);
                 }
				objArray[j] = new Object [] {icon,key,titleStore.get(key),priceStorage.get(key)};
			}
		} else {
			log.info("SEARCH :No item avalable for " + item);
			throw new RuntimeException( "No item avalable for " + item);
		}
		log.info("SEARCH data for item : " + item + " is - " + content);
		createDataDump(outputFile);
		return objArray;

	}
	private String getTodayDate() {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		return sdf.format(dt);
	}

	//https://docs.oracle.com/javase/7/docs/api/java/util/Hashtable.html
	//https://examples.javacodegeeks.com/core-java/io/java-write-to-file-example/
	private void createDataDump(String fileName) {
		// Create header
		String content = "ID | Item Title |  Price | Quantity | Auction Date | Place \n";
		// iterate a storage and get the data from all storage for same key.
		for (String key : titleStore.keySet()) {
			// create content as per header.
			content = content + key + " " + DELIMITER + " " + titleStore.get(key) + " " + DELIMITER + " "
					+ priceStorage.get(key) + " " + DELIMITER + " " + quantityStorage.get(key) + " " + DELIMITER
					+ " " + auctionDateStorage.get(key) + " " + DELIMITER
					+ " " + placeStore.get(key) + "\n";
		}
		System.out.println(content);
		writeToFile(fileName, content);
	}

	// https://www.javatpoint.com/java-string-split
	// extract title of product
	// store the title URL in map
	private void extractTitle(String content) {
		String [] datas = content.split("<div class=\"title\">");
		for(int i=1; i < datas.length; i++) {
			String data = datas[i].substring(0,datas[i].indexOf("</div>"));
			titleStore.put(ids[i-1], data.replace("<br>", ""));
		}

	}

	//extract the price of product
	// store the price URL in map
	private void extractPrice(String content) {
		String [] datas = content.split("<div class=\"price\">");
		for(int i=1; i < datas.length; i++) {
			String data = datas[i].substring(0,datas[i].indexOf("</div>"));
			priceStorage.put(ids[i-1], data.replace("<br>", ""));
		}

	}
	//extract the ID of product
	// store the ID URL in map
	private  void extractID(String content) {
		String [] datas = content.split("<div class=\"product-number\"><span>Product #: </span>");
		ids = new String[datas.length-1];
		for(int i=1; i < datas.length; i++) {
			String data = datas[i].substring(0,datas[i].indexOf("</div>"));

			ids[i-1] = data.replace("<br>", "");
		}

	}

	// extract the image of product
	// store the image URL in map
	private  void extractImage(String content) {
		String [] datas = content.split("<img class=\"lazy-load\" src=\"");
		System.out.println("datas 0 " + datas[0]);
		for(int i=1; i < datas.length; i++) {
			String data = datas[i].substring(0,datas[i].indexOf("\""));
			imageStore.put(ids[i-1], data);
		}

	}
	/**
	 * This method is used to write content into a file.
	 *
	 * @param fileName
	 * @param content
	 */
	 // https://www.mkyong.com/java/how-to-write-to-file-in-java-bufferedwriter-example/
	private void writeToFile(String fileName, String content) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			fw = new FileWriter(fileName);
			bw = new BufferedWriter(fw);
			bw.write(content);

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}

	//https://www.ntu.edu.sg/home/ehchua/programming/java/JavaLogging.html
	//https://docs.oracle.com/javase/7/docs/api/java/util/logging/Logger.html
	private Logger getLogger(String file) {
		Logger logger = Logger.getLogger(file);
		FileHandler fh;

		try {

			// This block configures the logger with handler and formatter
			fh = new FileHandler(file);
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logger;

	}

	// This method will read the logfiles
	// https://www.ntu.edu.sg/home/ehchua/programming/java/JavaLogging.html
	public String readLogFile() {
		StringBuilder sb = new StringBuilder();
		try {

			// This will reference one line at a time
			String line = null;

			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(logFile);

			// wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

			// close files.
			bufferedReader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
}
