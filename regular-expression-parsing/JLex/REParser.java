/*
    The MIT License (MIT)

    Copyright (c) 2009 Sam Gross, Adam Lerer and Ben Gelb

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/

package JLex;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class REParser 
{
    // Instance variables for command-line arguments.
    public static String LANGUAGE_MODE;
    public static String OUTPUT_PATH;
    public static String REGEX_NAME;
    public static String PATTERN_NAME;

    public static void main(String arg[]) throws IOException 
    {


	System.out.print("arg[0] = "+arg[0]+"\n");
	System.out.print("arg[1] = "+arg[1]+"\n");
	System.out.print("arg[2] = "+arg[2]+"\n");


        // Parse command-line arguments.
        // C, Verilog, or Bluespec.
        LANGUAGE_MODE = arg[0];
        // Output directory.
        OUTPUT_PATH = arg[1];
        // The input pattern.
        String[] regex_file = readFile(arg[2]).split("\n");
        // The regex name.
        REGEX_NAME = arg[3];
        
        // pattern name for error messages.
        PATTERN_NAME = arg[4];

        // Parse in the input file and generate a DFA.
        for (int i = 0; i< regex_file.length; i++)
        {
            if (!isRegexComment(regex_file[i]))
            {
                processRegex(regex_file[i]);
            }
        }
    }
        
    private static void processRegex(String regex) throws IOException
    {
        // Create a lex file of for JLex.
        String lex_file = OUTPUT_PATH + "/regex.lex";
        deleteFile(lex_file);
        deleteFile(lex_file + ".java");
        String lex = "import java.lang.System;\n\n%%\n\n%full\n\nALPHA=" + regex + "\n\n%%\n\n<YYINITIAL> {ALPHA} {\n  return 0;\n}";
        writeFile(lex_file, lex);
        // Invoke JLex on the lex file.
        Main.main(new String[]{lex_file});
        // Read in the result.
        String processedFilename = lex_file + ".java";
        String javaFile = readFile(processedFilename);
        // Remove what we don't need.
        Pattern p = Pattern.compile("\" \\+\r?\n\"");
        Matcher m = p.matcher(javaFile);
        javaFile = m.replaceAll("");
        // Uncomment this if you want the result:
        //System.out.println(javaFile);
        // Read the JLex-generated DFA in.
        int[] cmap = unpackArray("cmap",javaFile)[0];
        int[] rmap = unpackArray("rmap",javaFile)[0];
        int[][] next = unpackArray("nxt",javaFile);
        int[] accept = unpackAcpt(javaFile);
        // Uncomment this if you want to see raw results:
        //displayArray(new int[][]{accept});
        // In hardware we want 0 instead of -1 in the tables.
        arrayReplace(next,-1,0);
        // Uncomment this if you want to see the raw array:
        //displayArray(next);
        // Uncomment this if you want to try running the DFA in Java. Put an appropriate test string in to match against.
        //simulateDFA(next,cmap,rmap,accept,"THIS IS MY TEST STRING.\n I WILL REPLACE THIS WITH SOMETHING APPROPRIATE.");
        
        // Now that we've got the DFA, output it in the appropriate language.
        if (LANGUAGE_MODE.equals("C"))
        {
            String h_file = OUTPUT_PATH + "/" + REGEX_NAME + ".h";
            DFAtoC(next, cmap, rmap, accept, h_file);
        }
        else if (LANGUAGE_MODE.equals("Verilog"))
        {
            String v_file = OUTPUT_PATH + "/" + REGEX_NAME + ".v";
            DFAtoVerilog(next, cmap, rmap, accept, v_file);
        }
        else if (LANGUAGE_MODE.equals("Bluespec"))
        {
            String bsv_file = OUTPUT_PATH + "/" + REGEX_NAME + ".bsv";
            DFAtoBSV(next, cmap, rmap, accept, bsv_file);
        }
        else
        {
            System.out.println("Unrecognized language mode. Supported options are 'C' 'Verilog' and 'Bluespec'.");
        }
    }
    
    private static void DFAtoC(int[][] next, int[] cmap, int[] rmap, int[] accept, String filename) throws IOException
    {
        // This mess of code generates a C file with functions of the appropriate type.
        // I've tried to align things to make it more readable.
        String out = "";
        out = out.concat("namespace REGEX_MATCHER_" + PATTERN_NAME + " {\n\n");
        out = out.concat("#ifdef ENABLED_REGEX_" + PATTERN_NAME + "\n\n");
        out = out.concat("char charMap(char in){\n");
        out = out.concat("  switch( in ) {\n");
        for (int i = 0; i < 256; i++) //all 8-bit chars
        {
            int i2 = i < 128 ? i : i - 256;
            out = out.concat("    case " + i2 + ": return " + cmap[i] +";\n");
        }
        out = out.concat("    default: return 0;\n");
        out = out.concat("  }\n");
        out = out.concat("}\n\n");
        out = out.concat("REGEX_STATE stateMap(REGEX_STATE in){\n");
        out = out.concat("  switch( in ){\n");
        for (int i = 0; i < rmap.length; i++)
        {
            out = out.concat("    case " + i + ": return " + rmap[i] +";\n");
        }
        out = out.concat("    default: return 0;\n");
        out = out.concat("  }\n");
        out = out.concat("}\n\n");
        out = out.concat("bool acceptStates(REGEX_STATE in){\n");
        out = out.concat("  switch( in ){\n");
        for (int i = 0; i < accept.length; i++)
        {
            if (accept[i] != 0)
            {
                    out = out.concat("    case " + i + ": return true;\n");
            }
            else
            {
                    out = out.concat("    case " + i + ": return false;\n"); 
            }
        }
        out = out.concat("    default: return false;\n");
        out = out.concat("  }\n");
        out = out.concat("}\n\n");
        out = out.concat("REGEX_STATE stateTransition(REGEX_STATE mappedState, char mappedIn){\n");
        out = out.concat("  switch( mappedState ){\n");
	int row_length = next[0].length;
        for (int i = 0; i < next.length; i++) 
        {
            int[] row = next[i];
            out = out.concat("    case " + i + ": switch ( mappedIn ) {\n");

	    if(row_length != row.length){
		System.out.println("DFAtoC: false assumptions about state transition table\n");
		System.exit(-1);
	    }

            for (int j = 0; j < row.length; j++)
            {
                out = out.concat("      case " + j + ": return " + row[j] + ";\n");
            }

            out = out.concat("      default: return 0;\n");
            out = out.concat("    }\n");
        }
        out = out.concat("    default: return 0;\n");
        out = out.concat("  }\n");
        out = out.concat("}\n\n");

	out = out.concat("int numStates = "+rmap.length+";\n");
	out = out.concat("int numChars  = "+row_length+";\n\n");

        out = out.concat("static REGEX_MATCHER getRegexMatcher(charMap, stateMap, stateTransition, acceptStates, \"" + PATTERN_NAME + "\");\n\n");
        out = out.concat("#else\n\n");
        out = out.concat("static REGEX_MATCHER getRegexMatcher(\"" + PATTERN_NAME + "\");\n\n");
        out = out.concat("#endif\n\n");
        out = out.concat("}\n\n");
        
        writeFile(filename, out);
    }

    private static void DFAtoVerilog(int[][] next, int[] cmap, int[] rmap, int[] accept, String filename) throws IOException
    {
        // This is a copy-and-paste of the above, but for Verilog.
        String out = "";

        out = out.concat("module " + PATTERN_NAME + "_verilog(clk,\n");
        out = out.concat("                    rst_n,\n"); 
        out = out.concat("                    char_in,\n"); 
        out = out.concat("                    char_in_vld,\n"); 
        out = out.concat("                    state_in,\n"); 
        out = out.concat("                    state_in_vld,\n"); 
        out = out.concat("                    state_out,\n"); 
        out = out.concat("                    accept_out);\n");
        out = out.concat("   // The clock and reset info.\n");
        out = out.concat("    input clk, rst_n;\n");
        out = out.concat("    // Input character, and state, if being set.\n");
        out = out.concat("    input [7:0] char_in;\n");
        out = out.concat("    input [10:0] state_in;\n");
        out = out.concat("    // char_in_vld should be true if there's a character to process.\n");
        out = out.concat("    // state_in_vld should be true if the outside world is overwriting our state.\n");
        out = out.concat("    input char_in_vld, state_in_vld;\n");
        out = out.concat("    // state_out is our current state.\n");
        out = out.concat("    output [10:0] state_out;\n");
        out = out.concat("    // Accept out is true if the character triggered a regex match.\n");
        out = out.concat("    output accept_out;\n");

        out = out.concat("    // A register for the current state.\n");
        out = out.concat("    reg [10:0] cur_state;\n\n\n");
        out = out.concat("`ifdef ENABLED_REGEX_" + PATTERN_NAME + "\n\n");
        out = out.concat("function charMap;\n");
        out = out.concat("  input [7:0] inchar;\n");
        out = out.concat("  begin\n");
        out = out.concat("  case( inchar )\n");
        for (int i = 0; i < 256; i++) //all 8-bit chars
        {
            out = out.concat("    " + i + ": charMap = 8'd" + cmap[i] +";\n");
        }
        out = out.concat("    default: charMap = 8'bX;\n");
        out = out.concat("  endcase\n");
        out = out.concat("end\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("function stateMap;\n");
        out = out.concat("  input [10:0] instate;\n");
        out = out.concat("begin\n");
        out = out.concat("  case( instate )\n");
        for (int i = 0; i < rmap.length; i++)
        {
            out = out.concat("    " + i + ": stateMap = 11'd" + rmap[i] +";\n");
        }
        out = out.concat("    default: stateMap = 11'bX;\n");
        out = out.concat("  endcase\n");
        out = out.concat("end\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("function acceptStates;\n");
        out = out.concat("  input [10:0] instate;\n");
        out = out.concat("begin\n");
        out = out.concat("  case( instate )\n");
        for (int i = 0; i < accept.length; i++)
        {
            if (accept[i] != 0)
            {
                out = out.concat("    " + i + ": acceptStates = 1'b1;\n");
            }
            else
            {
                out = out.concat("    " + i + ": acceptStates = 1'b0;\n"); 
            }
        }
        out = out.concat("    default: acceptStates = 1'bX;\n");
        out = out.concat("  endcase\n");
        out = out.concat("end\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("function stateTransition;\n");
        out = out.concat("  input [10:0] mapped_state;\n");
        out = out.concat("  input [7:0]  mapped_char;\n");
        out = out.concat("begin\n");
        out = out.concat("  case( mapped_state )\n");
        for (int i = 0; i < next.length; i++) {
            int[] row = next[i];
            out = out.concat("    " + i + ": case ( mapped_char ) \n");
            for (int j = 0; j < row.length; j++)
            {
                    out = out.concat("      " + j + ": stateTransition = 11'd" + row[j] + ";\n");
            }
            out = out.concat("      default: stateTransition = 11'bX;\n");
            out = out.concat("    endcase\n");
        }
        out = out.concat("    default: stateTransition = 11'bX;\n");
        out = out.concat("  endcase\n");
        out = out.concat("end\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("`else\n\n");
        out = out.concat("function charMap;\n");
        out = out.concat("input [7:0] inchar;\n");
        out = out.concat("begin\n");
        out = out.concat("    charMap = inchar;\n");
        out = out.concat("end\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("function stateMap;\n");
        out = out.concat("input [10:0] instate;\n");
        out = out.concat("begin\n");
        out = out.concat("    stateMap = instate;\n");
        out = out.concat("end\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("function acceptStates;\n");
        out = out.concat("    input [10:0] instate;\n");
        out = out.concat("begin\n");
        out = out.concat("    acceptStates = 1'b0;\n");
        out = out.concat("end\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("function stateTransition;\n");
        out = out.concat("    input [10:0] instate;\n");
        out = out.concat("    input [7:0]  inchar;\n");
        out = out.concat("begin\n");
        out = out.concat("    stateTransition = instate;\n");
        out = out.concat("end\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("`endif\n\n");


        out = out.concat("    // Invoke the DFA functions.\n");    
        out = out.concat("    wire [7:0]  mapped_char;\n");
        out = out.concat("    wire [10:0] mapped_state, next_state;\n");
        out = out.concat("    wire next_accept;\n");

        out = out.concat("    assign mapped_char = charMap(char_in);\n");
        out = out.concat("    assign mapped_state = stateMap(cur_state);\n");
        out = out.concat("    assign next_state = stateTransition(mapped_state, mapped_char);\n");
        out = out.concat("    assign next_accept = acceptStates(next_state);\n");

        out = out.concat("    // Update our outputs.\n");
        out = out.concat("    assign accept_out = state_in_vld ? 1'b0 : char_in_vld ? next_accept : 1'b0;\n");
        out = out.concat("    assign state_out = cur_state;\n");
    
        out = out.concat("    // Update our local state.\n");
        out = out.concat("    always @(posedge clk)\n");
        out = out.concat("    begin\n");
        out = out.concat("       if (!rst_n)\n");
        out = out.concat("        begin\n");
        out = out.concat("            cur_state <= 0;\n");
        out = out.concat("        end\n");
        out = out.concat("        else\n");
        out = out.concat("        begin\n");
        out = out.concat("            if (state_in_vld)\n");
        out = out.concat("            begin\n");
        out = out.concat("                cur_state <= state_in;\n");
        out = out.concat("            end\n");
        out = out.concat("            else if (char_in_vld)\n");
        out = out.concat("            begin\n");
        out = out.concat("                cur_state <= next_state;\n");
        out = out.concat("            end\n");
        out = out.concat("        end\n");
        out = out.concat("    end\n");

        out = out.concat("endmodule\n");

        writeFile(filename, out);
    }

    private static void DFAtoBSV(int[][] next, int[] cmap, int[] rmap, int[] accept, String filename) throws IOException
    {
        // This is a copy-and-paste of the above, but for Bluespec.
        String out = "";
        out = out.concat("package REGEX_MATCHER_" + PATTERN_NAME + ";\n\n");
        out = out.concat("`ifdef ENABLED_REGEX_" + PATTERN_NAME + "\n\n");
        out = out.concat("function Bit#(8) charMap(Bit#(8) in);\n");
        out = out.concat("  case( in )\n");
        for (int i = 0; i < 256; i++) //all 8-bit chars
        {
            out = out.concat("    " + i + ": return " + cmap[i] +";\n");
        }
        out = out.concat("    default: return ?;\n");
        out = out.concat("  endcase\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("function Bit#(11) stateMap(Bit#(11) in);\n");
        out = out.concat("  case( in )\n");
        for (int i = 0; i < rmap.length; i++)
        {
            out = out.concat("    " + i + ": return " + rmap[i] +";\n");
        }
        out = out.concat("    default: return ?;\n");
        out = out.concat("  endcase\n");
        out = out.concat("endfunction\n\n");
        
        out = out.concat("function Bool acceptStates(Bit#(11) in);\n");
        out = out.concat("  case( in )\n");
        for (int i = 0; i < accept.length; i++)
        {
            if (accept[i] != 0)
            {
                out = out.concat("    " + i + ": return True;\n");
            }
            else
            {
                out = out.concat("    " + i + ": return False;\n"); 
            }
        }
        out = out.concat("    default: return ?;\n");
        out = out.concat("  endcase\n");
        out = out.concat("endfunction\n\n");
        
        out = out.concat("function Bit#(8) stateTransition(Bit#(11) mappedState, Bit#(8) mappedChar);\n");
        out = out.concat("  case( mappedState )\n");
        for (int i = 0; i < next.length; i++) {
            int[] row = next[i];
            out = out.concat("    " + i + ": case ( mappedChar ) \n");
            for (int j = 0; j < row.length; j++)
            {
                    out = out.concat("      " + j + ": return " + row[j] + ";\n");
            }
            out = out.concat("      default: return ?;\n");
            out = out.concat("    endcase\n");
        }
        out = out.concat("    default: return ?;\n");
        out = out.concat("  endcase\n");
        out = out.concat("endfunction\n\n");
        out = out.concat("module mkRegexMatcher (REGEX_MATCHER);\n");
        out = out.concat("    let m <- mkRegexMatcherReal(charMap, stateMap, stateTransition, acceptStates, \"" + PATTERN_NAME + "\");\n");
        out = out.concat("    return m;\n");
        out = out.concat("endmodule\n\n");
        out = out.concat("`else\n\n");
        out = out.concat("module mkRegexMatcher (REGEX_MATCHER);\n");
        out = out.concat("    let m <- mkRegexMatcherFake(\"" + PATTERN_NAME + "\");\n");
        out = out.concat("    return m;\n");
        out = out.concat("endmodule\n\n");
        out = out.concat("`endif\n\n");
        out = out.concat("endpackage\n\n");

        writeFile(filename, out);
    }

    private static void simulateDFA(int[][] next, int[] cmap, int[] rmap, int[] accept, String input)
    {
        // Simulate the DFA in Java directly on the given input string.
        int state = 0;
        for (int i = 0; i < input.length(); i++)
        {
            System.out.println(state + ((accept[state] == 1)?" - accept":""));
            state = next[rmap[state]][cmap[(int) input.charAt(i)]];
        }
        System.out.println(state + ((accept[state] == 1)?" - accept":""));
    }

    // ********* Helper functions ***********

    private static boolean isRegexComment(String line) 
    {
       // Remove null lines and comments.
       return line.startsWith("#") || (line.trim().length() == 0);
    }
    
    private static String readFile(String filename) throws IOException
    {
        // Helper function to read in a file.
        String data = "";
        FileReader processedFile = new FileReader(filename);
        int cur;
        while ((cur = processedFile.read()) != -1)
               data = data += (char) cur;
        return data;
    }

    private static void writeFile(String filename, String data) throws IOException
    {
        // Helper function to write out a file.
        PrintWriter outWriter = new PrintWriter(new BufferedWriter(new FileWriter(filename,false)));
        outWriter.write(data);
        outWriter.close();
    }
    
    private static void deleteFile(String fileName)
    {
        // Helper function to erase a file.
        File f = new File(fileName);
        boolean success = f.delete();
        if (!success)
           System.out.println("deleteFile unsuccessful");
    }

    private static void arrayReplace(int[][] array, int from, int to)
    {
        // Replace all instances of a number with a different number
        for (int i=0; i<array.length; i++)
        {
            int[] next1 = array[i];
            for (int j=0; j<next1.length; j++)
            {
                if (next1[j] == from) 
                {
                    next1[j] = to;
                }
            }
            // System.out.println();
        }
    }

    private static int[][] unpackArray(String arrayName, String file)
    {
        // Read in one of the JLex-generated arrays.
        Pattern p = Pattern.compile(arrayName + ".*\\((\\d+),(\\d+),\r?\n\"([^\"]*)\"");
        Matcher m = p.matcher(file);
        m.find();
        //System.out.println(m.group(1)+","+m.group(2)+","+m.group(3));
        return unpackFromString(Integer.valueOf(m.group(1)),Integer.valueOf(m.group(2)),m.group(3));
    }
    
    private static int[] unpackAcpt(String file)
    {
        // Read in the list of accept states from JLex.
        int count = 0;
        List<Integer> res = new ArrayList<Integer>();
        while (true)
        {
            Pattern p = Pattern.compile("/\\* " + count + " \\*/ ([^,\r\n]*)");
            Matcher m = p.matcher(file);
            if (!m.find())
            {
                    break;
            }
            if (m.group(1).equals("YY_NOT_ACCEPT"))
            {
                    res.add(0);
            }
            else
            {
                    res.add(1);
            }
            count++;
        }
        int[] res2 = new int[res.size()];
        for (int i = 0; i < res.size(); i++)
        {
            res2[i] = res.get(i);
        }
        return res2;
    }

    private static int[][] unpackFromString(int size1, int size2, String st) 
    {
        // JLex compresses arrays into representations like 1,2,4:7,9,10.
        // This translates that.
        int colonIndex = -1;
        String lengthString;
        int sequenceLength = 0;
        int sequenceInteger = 0;

        int commaIndex;
        String workString;

        int res[][] = new int[size1][size2];
        for (int i= 0; i < size1; i++) {
            for (int j= 0; j < size2; j++) 
            {
                if (sequenceLength != 0) 
                {
                    res[i][j] = sequenceInteger;
                    sequenceLength--;
                    continue;
                }
                commaIndex = st.indexOf(',');
                workString = (commaIndex==-1) ? st :
                        st.substring(0, commaIndex);
                st = st.substring(commaIndex+1);
                colonIndex = workString.indexOf(':');
                if (colonIndex == -1) 
                {
                    res[i][j]=Integer.parseInt(workString);
                    continue;
                }
                lengthString = workString.substring(colonIndex+1);
                sequenceLength = Integer.parseInt(lengthString);
                workString = workString.substring(0,colonIndex);
                sequenceInteger = Integer.parseInt(workString);
                res[i][j] = sequenceInteger;
                sequenceLength--;
            }
        }
        return res;
    }
        
    private static void displayArray(int[][] array)
    {
        // Print out an array after reading it in.
        for (int i=0; i<array.length; i++)
        {
            int[] next1 = array[i];
            for (int j=0; j<next1.length; j++)
            {
                System.out.print(next1[j] + " ");
            }
            System.out.println();
        }
    }
    
}
