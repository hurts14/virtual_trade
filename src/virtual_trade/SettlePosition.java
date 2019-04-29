package virtual_trade;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Stream;

public class SettlePosition {

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		int count = 0;
		boolean flag = false;

		int LIMIT = 10;    //諦めて精算するまでの日数(day)
		int WIDTH = 500;   //スプレットの幅及び日経平均の上下幅(yen)
		int ERROR = 7;
		int nikkei = 0;
		int nikkei_abs = 0;
		int pl = 0;
		String n;
		String instruction;
		String day;
		String out = "";
		String sell1 = "",sell2 = "",buy1 = "", buy2 = "";;

		VirtualTrade_IntraDay v = new VirtualTrade_IntraDay();
		ArrayList<String> StrArray = new ArrayList<String>();
		ArrayList<String> StrArray2 = new ArrayList<String>();
		ArrayList<String> Output = new ArrayList<String>();
		System.out.println("START PROGRAM.	" + (new Date()));

		try (Stream<String> stream = Files.lines(Paths.get("Logi_-2_predict_date.csv"), StandardCharsets.UTF_8)) {
		    stream.forEach(line -> {
		    	StrArray.add(line);
		    });
		}catch(IOException e){
			e.printStackTrace();
		}
		try (Stream<String> stream = Files.lines(Paths.get("setShortButterfly-call.csv"), StandardCharsets.UTF_8)) {
		    stream.forEach(line -> {
		    	StrArray2.add(line);
		    });
		}catch(IOException e){
			e.printStackTrace();
		}
		v.setSQL("ume", "umeume");
		for(String str : StrArray2){
			String ss = str.split(",")[0];
			count = 0;
			for(String s : StrArray){
				instruction = s.split(",")[2];
				n = s.split(",")[1];
				day = s.split(",")[0];
				v.setInstruction(day, n, instruction);
				nikkei = Integer.parseInt(n);
				nikkei_abs = Math.abs(Integer.parseInt(str.split(",")[1])-nikkei);
				if(day.equals(ss)){
					flag = true;
				}else{
					if(flag){
						if((nikkei_abs >= WIDTH)||(count >= LIMIT)){
							//精算
							out = str.split(",")[0] + "," + str.split(",")[1] + "," + str.split(",")[3] + "," + str.split(",")[5];
							sell1 = str.split(",")[4];
							sell2 = str.split(",")[6];
							buy1 = str.split(",")[2];
							buy2 = buy1;
							pl = Integer.parseInt(str.split(",")[6]);
							//atC,atP,p/l
							String p = v.settelShortButterfly(sell1, sell2, buy1, buy2, pl, ERROR);
							out += "," + day + "," + n + "," + p;
							flag = false;
							Output.add(out);
						}else{
							count++;
						}
					}else{
					}
				}
			}
		}
		try{
			FileWriter fw = new FileWriter("ShortButterfly_Call.csv", true);
			//FileWriter fw = new FileWriter("LongStraddle.csv", true);
					PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
					for(String line : Output){
						pw.println(line);
					}
					System.out.println("finish output");
					pw.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		System.out.println("END PROGRAM.	" + (new Date()));
	}

}
