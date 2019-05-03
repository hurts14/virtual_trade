package virtual_trade;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.stream.Stream;

public class CheckBrand {

	public static Statement getStatement() {
				Connection con = null;
				Statement stm = null;
				String sqlusername = "ume";
				String sqlpass = "umeume";
				String PATH = "jdbc:mysql://localhost/putcall";
				String DriverName = "com.mysql.cj.jdbc.Driver";
				try {
						Class.forName(DriverName);
						con = DriverManager.getConnection(PATH,sqlusername,sqlpass);
						stm = con.createStatement();
				} catch (ClassNotFoundException e) {
						e.printStackTrace();
				} catch (SQLException e) {
						e.printStackTrace();
				}
				return stm;
		}
	public static ArrayList<String> getSelect(String select){
		//sqlのセレクト文
		Statement stm = null;
		ResultSet rs = null;
		ArrayList<String> StrArray = new ArrayList<String>();
		String s = select;
		try{
			stm = getStatement();
			rs = stm.executeQuery(s);

			while(rs.next()){
				String daet = rs.getString("Trade_Date");
				String code = rs.getString("Security_Code");
	      String time = rs.getString("Time");
	      String price = rs.getString("Trade_Price");
	      StrArray.add(daet + "," + code + "," + time + "," + price);
			}
		}catch(SQLException e){
			e.printStackTrace();
			StrArray.add("0,0,0,0" );
		}
		return StrArray;
	}

	public static void main(String[] args) {
		ArrayList<String> StrArray3 = new ArrayList<String>();
		String day = "2016-1-4";
		String date = "2016-1-7";
		String call = "141028818";
		String put = "131028818";
		int count = 0;

		String select01 = "SELECT Trade_Date,Security_Code,Time,Trade_Price FROM tick01 WHERE(Trade_Date = '";
		String select02 = "')AND(Security_Code = '";
		String select03 = "')AND(Time like '09%');";

		String select1 = "",select2 = "",select3 = "",select4 = "";
		// TODO 自動生成されたメソッド・スタブ
		System.out.println(getStatement());

		try (Stream<String> stream = Files.lines(Paths.get("notLS.csv"), StandardCharsets.UTF_8)) {
		    stream.forEach(line -> {
		    	StrArray3.add(line);
		    });
		}catch(IOException e){
			e.printStackTrace();
		}

		for(String line:StrArray3){
			day = line.split(",")[0];
			call = line.split(",")[9];
			put = line.split(",")[10];
			date = line.split(",")[4];

			select1 = select01 + day + select02 + call + select03;
			select2 = select01 + day + select02 + put + select03;
			select3 = select01 + date + select02 + call + select03;
			select4 = select01 + date + select02 + call + select03;

			ArrayList<String> StrArray = getSelect(select1);
			ArrayList<String> StrArray2 = getSelect(select2);
			ArrayList<String> StrArray4 = getSelect(select3);
			ArrayList<String> StrArray5 = getSelect(select4);

			try{
				String day2 = day.replaceAll("/", "-");
				String date2 = date.replaceAll("/", "-");
				FileWriter fw = new FileWriter("set"+day2+"settle"+date2+".csv", true);
				PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
				for(String str : StrArray){
					pw.println(str);
				}
				for(String str2 : StrArray2){
					pw.println(str2);
				}
				for(String str3 : StrArray4){
					pw.println(str3);
				}
				for(String str4 : StrArray5){
					pw.println(str4);
				}
				 System.out.println("finish output");
				 pw.close();
				}catch(IOException ex){
					ex.printStackTrace();
				}
		}
	}
}
