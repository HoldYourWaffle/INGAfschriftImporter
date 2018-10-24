package info.zthings.bankimport;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

public class Parser {
	
	public static void main(String[] args) throws IOException, SQLException {
		if (args.length < 2) {
			System.err.println("Usage: <csv_path> <jdbc_connection_string>");
			System.err.println("Connection string example (see JDBC documentation): jdbc:mysql://localhost/afschriften?user=username&password=secure&serverTimezone=Europe/Amsterdam&characterEncoding=UTF-8");
			System.exit(1);
		}
		ProgressBarBuilder pbb = new ProgressBarBuilder().setStyle(ProgressBarStyle.ASCII).setUpdateIntervalMillis(10).setPrintStream(System.out);
		
		FileReader in = new FileReader(args[0]);
		CSVParser records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
		List<Afschrift> afschriften = new ArrayList<>();
		
		pbb.setTaskName("Parsing csv records");
		for (CSVRecord record : ProgressBar.wrap(records.getRecords(), pbb)) {
			afschriften.add(new Afschrift(record.get(0), record.get(1), record.get(2), record.get(3), record.get(4), record.get(5), record.get(6), record.get(7), record.get(8)));
		}
		Collections.reverse(afschriften); // insert old records before new ones
		
		try (Connection db = DriverManager.getConnection(args[1]);
			 PreparedStatement stat = db.prepareStatement("INSERT INTO `afschriften` (`date`, `name`, `account_a`, `account_b`, `code`, `amount`, `kind`, `description`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
			
			pbb.setTaskName("Inserting into database");
			for (Afschrift afschrift : ProgressBar.wrap(afschriften, pbb)) {
				stat.setObject(1, afschrift.date);
				stat.setString(2, afschrift.name);
				stat.setString(3, afschrift.account_a);
				stat.setString(4, afschrift.account_b);
				stat.setString(5, afschrift.code);
				stat.setDouble(6, afschrift.amount);
				stat.setString(7, afschrift.kind);
				stat.setString(8, afschrift.description);
				stat.executeUpdate();
			}
		}
	}
	
	private static class Afschrift {
		public final LocalDate date;
		public final String name, account_a, account_b, code, kind, description;
		public final double amount;
		
		public Afschrift(String date, String name, String account_a, String account_b, String code, String add_sub, String amount, String kind, String description) {
			this.name = name;
			this.account_a = account_a;
			this.account_b = account_b;
			this.code = code;
			this.kind = kind;
			this.description = description;
			
			this.amount = Double.valueOf(amount.replace(',', '.')) * addSubFactor(add_sub); // java doesn't like ',' as the decimal seperator
			this.date = LocalDate.parse(date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8), DateTimeFormatter.ofPattern("yyyy-MM-dd")); // the date parts have to be ugily split
		}
		
		public static int addSubFactor(String str) {
			if (str.equalsIgnoreCase("Af")) return -1;
			else if (str.equalsIgnoreCase("Bij")) return 1;
			else throw new Error("Unknown af/bij-value " + str);
		}
	}
	
}
