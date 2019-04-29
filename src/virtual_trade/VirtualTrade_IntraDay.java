package virtual_trade;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

public class VirtualTrade_IntraDay {
	private String sqlusername;
	private String sqlpass;
	private String PATH = "jdbc:mysql://localhost/putcall";
	private String DriverName = "com.mysql.cj.jdbc.Driver";

	private int seal1;//値段
	private int seal2;
	private int buying1;
	private int buying2;
	private int ans = 0;
	private int ans2;
	private String buy1 = "";//銘柄
	private String buy2 = "";
	private String sell1 = "";
	private String sell2 = "";
	private String price;
	private String nikkei;
	private String day;
	private String instruction;
	ArrayList<String> StrArray = new ArrayList<String>();

	public int getAtTheMoney(){
		//atTheMoneyの値段を得る。
		int price = Integer.parseInt(nikkei);
		int a = price/125;
		int b = price%125;
		if(63>=b) return 125*a;
		return 125*(a+1);
	}
	public int getPriceWidth(int atTheMoney,int width,boolean plus){
		// true = +width,false = -width
		int price = atTheMoney;
		int w = width;
		if(plus) return price+w;
		return price-w;
	}
	public String getBrand(int p,boolean o){
		//銘柄の作成
		// true = put,false = call
		String option = "4";//DF = call
		String brand = "1";
		String y="0",year="",month="",price="";
		String g ="";

		if(o) option = "3";//put

		try{
			year = day.split("/")[0];
			//~,2014=9,2015=0,2016=1,~
			if("2014".equals(year)) y = "9";
			else if("2015".equals(year)) y = "0";
			else if("2016".equals(year)) y = "1";
			else if("2017".equals(year)) y = "2";
			else if("2018".equals(year)) y = "3";
			else if("2019".equals(year)) y = "4";
			else if("2020".equals(year)) y = "5";
			else if("2013".equals(year)) y = "8";
			else if("2012".equals(year)) y = "7";
			else y = "6";
			//その月の次の限月にする
			month = day.split("/")[1];
			int m = Integer.parseInt(month) + 1;
			if(13 == m){
				m = 1;
				int x = Integer.parseInt(y) + 1;
				if(x == 10) x = 0;
				y = String.valueOf(x);
			}
			g = String.valueOf(m);
			if(1 >= g.length()) g = "0" + g;
		}catch(ArrayIndexOutOfBoundsException e){

		}
		String s = String.valueOf(p);//int>>>Stirng
		price = s.substring(1, 3);//12345>>>23

		brand += (option + y + g + price + "18");

		return brand;
	}
	public String getRecord(String Sell1,String Sell2,int alpha){
		String select02 = "')AND(Security_Code = '";
		String select03 = "')AND(Time like '09%');";
		String select04 = "SELECT Time,Trade_Price FROM tick01 WHERE (Price_Type = 'o')AND(Trade_Date = '";
		String select05 = "')AND(Time like '09%')AND(";
		String select06 = "SELECT Time,Trade_Price FROM tick01 WHERE (Price_Type = 'n')AND(Trade_Date = '";
		String select07 = "%' AND '09";
		String select08 = "%') limit 1;";
		String select1 = "",select2 = "";
		select1 = select04 + day + select02 + Sell1 + select03;
		select2 = select04 + day + select02 + Sell2 + select03;
		int error = alpha;

		String sell1 = this.getSelect(select1);
		String sell2 = this.getSelect(select2);

		String sell1_time;
		String sell1_price;

		String sell2_time;
		String sell2_price;

		try{
			sell1_time = sell1.split(",")[0];
			sell1_price = sell1.split(",")[1];

			sell2_time = sell2.split(",")[0];
			sell2_price = sell2.split(",")[1];
		}catch(ArrayIndexOutOfBoundsException e){
			sell1_time = "000000000";
			sell2_time = "000000000";
			sell1_price = "0";
			sell2_price = "0";
		}

		if(this.comperToerror(sell1_time, sell2_time, error)){
			price = sell1_price + ',' + sell2_price;
			return price;
		}else{
			int t1 = Integer.parseInt(sell1_time.substring(2, 4));
			int t2 = Integer.parseInt(sell2_time.substring(2, 4));
			int m = Math.max(t1, t2);
			String time1 = String.valueOf(m+error);
			String time2 = String.valueOf(m-error);
			//中値取ってSQL
			if(t1 == m){
				select2 = select06 + day + select02 + Sell2 + select05 + time1 + select07 + time2 + select08;
				sell2 = this.getSelect(select2);
				try{
					sell2_time = sell1.split(",")[0];
					sell2_price = sell1.split(",")[1];
				}catch(ArrayIndexOutOfBoundsException e){
					sell2_time = "000000000";
					sell2_price = "0";
				}
			}else{
				select1 = select06 + day + select02 + Sell1 + select05 + time1 + select07 + time2 + select08;
				sell1 = this.getSelect(select1);
				try{
					sell1_time = sell1.split(",")[0];
					sell1_price = sell1.split(",")[1];
				}catch(ArrayIndexOutOfBoundsException e){
					sell1_time = "000000000";
					sell1_price = "0";
				}
			}
			if(this.comperToerror(sell1_time, sell2_time, error)){
				price = sell1_price + ',' + sell2_price;
				return price;
			}
			price = "-1,-1";
			return price;
		}
	}
	public String getRecord(String Sell1,String Sell2,String Buy1,String Buy2,int alpha){
		//String select01 = "SELECT Trade_Date,Security_code,Time,Trade_Price FROM tick01 WHERE (Price_Type = 'o')AND(Trade_Date = '";
		String select02 = "')AND(Security_Code = '";
		String select03 = "')AND(Time like '09%');";
		String select04 = "SELECT Time,Trade_Price FROM tick01 WHERE (Price_Type = 'o')AND(Trade_Date = '";
		String select05 = "')AND(Time like '09";
		String select06 = "SELECT Time,Trade_Price FROM tick01 WHERE (Price_Type = 'n')AND(Trade_Date = '";
		String select07 = "%');";
		String select1 = "",select2 = "",select3 = "",select4 = "";
		select1 = select04 + day + select02 + Sell1 + select03;
		select2 = select04 + day + select02 + Sell2 + select03;
		select3 = select04 + day + select02 + Buy1 + select03;
		select4 = select04 + day + select02 + Buy2 + select03;
		//基準時間から誤差αの値段をとる
		//4つの銘柄のオープニングプライス、時間を取る
		String sell1 = this.getSelect(select1);
		String sell2 = this.getSelect(select2);
		String buy1 = this.getSelect(select3);
		String buy2 = this.getSelect(select4);
		String sell1_time = "0";
		String sell1_price = "0";
		String sell2_time = "0";
		String sell2_price = "0";
		String buy1_time = "0";
		String buy1_price = "0";
		String buy2_time = "0";
		String buy2_price = "0";

		try{
			sell1_time = sell1.split(",")[0];
			sell1_price = sell1.split(",")[1];

			sell2_time = sell2.split(",")[0];
			sell2_price = sell2.split(",")[1];

			buy1_time = buy1.split(",")[0];
			buy1_price = buy1.split(",")[1];

			buy2_time = buy2.split(",")[0];
			buy2_price = buy2.split(",")[1];
		}catch(ArrayIndexOutOfBoundsException e){
			sell1_time = "000000000";
			sell2_time = "000000000";
			sell1_price = "0";
			sell2_price = "0";
			buy1_time = "000000000";
			buy1_price = "0";
			buy2_time = "000000000";
			buy2_price = "0";
		}

		int error = alpha;
		//時間見て、誤差以内ならpriceに渡す
		if((this.comperToerror(buy1_time, buy2_time, error))&&(this.comperToerror(buy1_time, sell1_time, error))&&(this.comperToerror(sell1_time, sell2_time, error))){
			price = sell1_price + ',' + sell2_price + "," + buy1_price + "," + buy2_price;
			//sell1,sell2,buy1,buy2の順
			return price;
		}else{
			/*//無理なら一番遅い時間での中値を取る。
			int t1 = Integer.parseInt(buy1_time.substring(2, 4));
			int t2 = Integer.parseInt(buy2_time.substring(2, 4));
			int m = Math.max(t1, t2);
			m = Math.max(m, Integer.parseInt(sell1_time.substring(2, 4)));
			m = Math.max(m, Integer.parseInt(sell2_time.substring(2, 4)));
			String time = String.valueOf(m);
			select1 = select06 + day + select02 + Sell1 + select05 + time + select07;
			select2 = select06 + day + select02 + Sell2 + select05 + time + select07;
			select3 = select04 + day + select02 + Buy1 + select05 + time + select07;
			select4 = select04 + day + select02 + Buy2 + select05 + time + select07;

			sell1 = this.getSelect(select1);
			sell2 = this.getSelect(select2);
			buy1 = this.getSelect(select3);
			buy2 = this.getSelect(select4);
			try{
				sell1_time = sell1.split(",")[0];
				sell1_price = sell1.split(",")[1];

				sell2_time = sell2.split(",")[0];
				sell2_price = sell2.split(",")[1];

				buy1_time = buy1.split(",")[0];
				buy1_price = buy1.split(",")[1];

				buy2_time = buy2.split(",")[0];
				buy2_price = buy2.split(",")[1];
			catch(ArrayIndexOutOfBoundsException e){
				sell1_time = "000000000";
				sell2_time = "000000000";
				sell1_price = "0";
				sell2_price = "0";
				buy1_time = "000000000";
				buy1_price = "0";
				buy2_time = "000000000";
				buy2_price = "0";
			}


			if((this.comperToerror(buy1_time, buy2_time, error))&&(this.comperToerror(buy1_time, sell1_time, error))&&(this.comperToerror(sell1_time, sell2_time, error))){
				price = sell1_price + ',' + sell2_price + "," + buy1_price + "," + buy2_price;
				//sell1,sell2,buy1,buy2の順
				return price;
			}else{
				//無かったら-1にする。*/
				price = "-1,-1,-1,-1";
				return price;
			//}
		}
	}
	public boolean comperToerror(String time1,String time2,int error){
		//誤差の範囲内かの判定
		int t1 = Integer.parseInt(time1.substring(2, 4));
		int t2 = Integer.parseInt(time2.substring(2, 4));
		int t3 = error;
		boolean ans = true;
		if(t3 >= Math.abs(t1-t2)) return ans;
		ans = false;
		return ans;
	}
	public String setLongStraddle(int ERROR){
		int error = ERROR;
		int payment;
		String atCall,atPut;

		atCall = this.getBrand(this.getAtTheMoney(), false);
		atPut = this.getBrand(this.getAtTheMoney(), true);
		payment = this.makeLongStraddle(atCall,atPut,error);

		int s1p = this.getSale1();
		int s2p = this.getSale2();

		price = atCall + "," +String.valueOf(s1p) + "," + atPut +","+ String.valueOf(s2p)+",";
		price += String.valueOf(payment);
		return price;
	}
	public String setShortButterfly(int WIDTH,int ERROR){
		/*
		 * 価格取って、計算して、Stringでも返す。
		 */
		int widhe = WIDTH;
		int error = ERROR;
		int payment;
		String atCall,atPut,inCall,inPut,outCall,outPut;
		//建てるフェーズ
		//call,putのat,in,outを得る
		atCall = this.getBrand(this.getAtTheMoney(), false);
		atPut = this.getBrand(this.getAtTheMoney(), true);
		inCall = this.getBrand(this.getPriceWidth(this.getAtTheMoney(), widhe, false), false);
		inPut = this.getBrand(this.getPriceWidth(this.getAtTheMoney(), widhe, true), true);
		outCall = this.getBrand(this.getPriceWidth(this.getAtTheMoney(), widhe, true), false);
		outPut = this.getBrand(this.getPriceWidth(this.getAtTheMoney(), widhe, false), true);
		//makeShortButterflyで値段得る
		//変える
		payment = this.makeShortButterfly(inCall, outCall, atCall, atCall,error);

		// 各値段の取得
		int s1p = this.getSale1();
		int s2p = this.getSale2();
		int b1p = this.getBuying1();
		//int b2p = this.getBuying2();
		price = inCall + "," + String.valueOf(b1p)+ "," + outCall + "," + String.valueOf(s1p) + "," + atCall + "," +String.valueOf(s2p);
		//price += atCall+","+b2p;
		price += String.valueOf(payment);
		return price;
	}

	public int makeShortButterfly(String Sell1,String Sell2,String Buy1,String Buy2,int error){
		//値段取って計算
		sell1 = Sell1;
		sell2 = Sell2;
		buy1 = Buy1;
		buy2 = Buy2;
		int e = error;

		String str = this.getRecord(sell1, sell2, buy1, buy2, e);
		seal1 = Integer.parseInt(str.split(",")[0]);
		seal2 = Integer.parseInt(str.split(",")[1]);
		buying1 = Integer.parseInt(str.split(",")[2]);
		buying2 = Integer.parseInt(str.split(",")[3]);


		ans = (buying1 + buying2) - (seal1 + seal2);
		return ans;
	}
	public int makeLongStraddle(String Sell1,String Sell2,int error){
		sell1 = Sell1;
		sell2 = Sell2;
		int e = error;

		String str = this.getRecord(Sell1, Sell2, e);

		seal1 = Integer.parseInt(str.split(",")[0]);
		seal2 = Integer.parseInt(str.split(",")[1]);
		ans = seal1 + seal2;

		return ans;
	}
	public int settelShortButterfly(int error){
		//精算フェーズ
		//値段取って計算
		//日付をみる
		int year  = Integer.parseInt(day.split("/")[0]);
		int month = Integer.parseInt(day.split("/")[1]) - 1;
		int date  = Integer.parseInt(day.split("/")[2]);
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, date);
		int d = cal.get(Calendar.DAY_OF_WEEK);
		int f = Calendar.FRIDAY;
		if((f == d)&&(8 <= date) && (14 >= date)){
			//sq
			ans2 = -1;
		}else{
			int e = error;
			String str = this.getRecord(sell1, sell2, buy1, buy2, e);
			seal1 = Integer.parseInt(str.split(",")[0]);
			seal2 = Integer.parseInt(str.split(",")[1]);
			buying1 = Integer.parseInt(str.split(",")[2]);
			buying2 = Integer.parseInt(str.split(",")[3]);

			ans2 = (buying1 + buying2) - (seal1 + seal2);
		}
		return ans2;
	}
	public String settleLongStrddle(String sell1,String sell2,int pl,int e){
		String str = "";
		String atCall = sell1;
		String atPut = sell2;
		int x = pl;
		int er = e;
		int payment;
		payment = this.makeLongStraddle(atCall,atPut,er) - x;
		int s1p = this.getSale1();
		int s2p = this.getSale2();

		str = String.valueOf(s1p) + "," + String.valueOf(s2p)+",";
		str += String.valueOf(payment);

		return str;
	}
	public void setSQL(String name,String pass){
		//sqlのユーザネームとパスワードのセット
		sqlusername = name;
		sqlpass = pass;
	}
	public void setInstruction(String Day,String Nikkei,String Instruction){
		//1
		instruction = Instruction;
		nikkei = Nikkei;
		day = Day;
	}
	public int getBuying1(){
		//値段
		return buying1;
	}
	public int getBuying2(){
		return buying2;
	}
	public int getSale1(){
		return seal1;
	}
	public int getSale2(){
		return seal2;
	}
	//sql
	public Statement getStatement() {
        Connection con = null;
        Statement stm = null;
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
	public String getSelect(String select){
		//sqlのセレクト文
		Statement stm = null;
		ResultSet rs = null;
		String line = "";
		String s = select;
		try{
			stm = this.getStatement();
			rs = stm.executeQuery(s);

			while(rs.next()){
	            String time = rs.getString("Time");
	            String price = rs.getString("Trade_Price");
	            line = (time + "," + price);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return line;
	}
}
