package virtual_trade;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class ShortButterfly_Call {

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ

		int LIMIT = 10;    //諦めて精算するまでの日数(day)
		int WIDTH = 500;   //スプレットの幅及び日経平均の上下幅(yen)
		int ERROR = 5;     //誤差の時間(min)

		int payment = 0,pl = 0,count = 0;
		boolean positon = false;
		boolean ins = false;
		String atCall,atPut,inCall,inPut,outCall,outPut;
		String price;
		String result = "";
		VirtualTrade_IntraDay v = new VirtualTrade_IntraDay();
		ArrayList<String> StrArray = new ArrayList<String>();
		ArrayList<String> Output = new ArrayList<String>();

		try (Stream<String> stream = Files.lines(Paths.get("Logi_-2_predict_date.csv"), StandardCharsets.UTF_8)) {
		    stream.forEach(line -> {
		    	StrArray.add(line);
		    });
		}catch(IOException e){
			e.printStackTrace();
		}
		v.setSQL("ume", "umeume");
		//売買指示が出たら絶対ポジションを建てる。
		for(String str : StrArray){
			try{
				String instruction = str.split(",")[2];
				String nikkei = str.split(",")[1];
				String day = str.split(",")[0];
				v.setInstruction(day, nikkei, instruction);
				int nikkeiindex = Integer.parseInt(nikkei);
				/*
				 * 0は無視、1のときの次の日に建てる
				 * カウンター、日付、日経平均、各銘柄コード、各値段、p/l
				 */
				if(positon){
					if(WIDTH <= nikkeiindex){
						//精算フェーズ
						//p/l = ans-ans2
						pl = v.settelShortButterfly(ERROR) - payment;
						//
						int s1p = v.getSale1();
						int s2p = v.getSale2();
						int b1p = v.getBuying1();
						//int b2p = v.getBuying2();
						price = String.valueOf(b1p) + "," + String.valueOf(s1p) + "," +String.valueOf(s2p);
						//日付,日経平均,at,in,out,p/l
						result += "," + day +","+ nikkei +"," + price +","+ String.valueOf(pl);
						Output.add(result);

						count = 0;
						positon = false;
					}else{
						++count;
						if(LIMIT == count){
							//諦めて精算
							//精算フェーズ
							//p/l = ans-ans2
							pl = v.settelShortButterfly(ERROR) - payment;
							//
							int s1p = v.getSale1();
							int s2p = v.getSale2();
							int b1p = v.getBuying1();
							//int b2p = v.getBuying2();
							price = String.valueOf(b1p) + "," + String.valueOf(s1p) + "," +String.valueOf(s2p);
							//日付,日経平均,at,in,out,p/l
							result += "," + day +","+ nikkei +"," + price +","+ String.valueOf(pl);
							Output.add(result);

							count = 0;
							positon = false;
						}
					}
				}else{
					if("1".equals(instruction)){
						ins = true;
					}else{
						if(ins){
							//建てるフェーズ
							//call,putのat,in,outを得る
							atCall = v.getBrand(v.getAtTheMoney(), false);
							atPut = v.getBrand(v.getAtTheMoney(), true);
							inCall = v.getBrand(v.getPriceWidth(v.getAtTheMoney(), WIDTH, false), false);
							inPut = v.getBrand(v.getPriceWidth(v.getAtTheMoney(), WIDTH, true), true);
							outCall = v.getBrand(v.getPriceWidth(v.getAtTheMoney(), WIDTH, true), false);
							outPut = v.getBrand(v.getPriceWidth(v.getAtTheMoney(), WIDTH, false), true);
							//makeShortButterflyで値段得る
							payment = v.makeShortButterfly(inCall, outCall, atCall, atCall,ERROR);

							int s1p = v.getSale1();
							int s2p = v.getSale2();
							int b1p = v.getBuying1();
							//int b2p = v.getBuying2();
							price = String.valueOf(b1p) + "," + String.valueOf(s1p) + "," +String.valueOf(s2p);

							//日付,日経平均,at,in,out
							result = day +","+ String.valueOf(nikkei) +"," + price +",";
							positon = true;
							ins = false;
						}else{
							//次の日
						}
					}
				}
			}catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}
		}

		try{
			FileWriter fw = new FileWriter("ShortButterfly_Call.csv", true);
	        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
	        for(String str : Output){
	        	pw.println(str);
	        }
	        System.out.println("finish output");
	        pw.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}

	}

}
