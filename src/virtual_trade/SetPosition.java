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
import java.util.Date;

public class SetPosition {

	public static void main(String[] args) {

		int LIMIT = 10;    //諦めて精算するまでの日数(day)
		int WIDTH = 500;   //スプレットの幅及び日経平均の上下幅(yen)
		int ERROR = 5;     //誤差の時間(min)

		String nikkei = "";
		String day = "";
		String instruction = "";
		boolean flag = false;
		String BUTTERFLY = "ShortButterfly";
		String STRADDLE = "LongStraddle";

		VirtualTrade_IntraDay v = new VirtualTrade_IntraDay();
		ArrayList<String> StrArray = new ArrayList<String>();
		ArrayList<String> Output = new ArrayList<String>();

		System.out.println("START PROGRAM.	" + (new Date()));
		try (Stream<String> stream = Files.lines(Paths.get("Logi_-2_predict_date.csv"), StandardCharsets.UTF_8)) {
		    stream.forEach(line -> {
		    	StrArray.add(line);
		    });
		}catch(IOException e){
			e.printStackTrace();
		}
		v.setSQL("ume", "umeume");
		for(String str : StrArray){
			try{
				instruction = str.split(",")[2];
				nikkei = str.split(",")[1];
				day = str.split(",")[0];
				v.setInstruction(day, nikkei, instruction);
			}catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}

			if(flag){
				String s = v.setLongStraddle(ERROR);
				//String s = v.setShortButterfly(WIDTH, ERROR);
				s = day + "," + nikkei + "," + s;
				Output.add(s);
				flag = false;
			}

			if("1".equals(instruction)){
				flag = true;
			}else{
				//miss
			}
		}

		try{
			FileWriter fw = new FileWriter("set"+STRADDLE+".csv", true);
	        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
	        for(String str : Output){
	        	pw.println(str);
	        }
	        System.out.println("finish output");
	        pw.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		System.out.println("END PROGRAM.	" + (new Date()));

	}
}
