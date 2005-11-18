package langs;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class LangsTask extends Task
{
	private String languages, inDir, outDir;

	static private String[] devideToWords(String text)
	{
		Vector result = new Vector();
		int curIdx = 0, size, startIdx;
		char eos = 0;
		
		size = text.length();
		for (;;)
		{
			// searching begin of word
			for (; curIdx < size; curIdx++) if (text.charAt(curIdx) != ' ') break;
			if (curIdx >= size) break;
			
			startIdx = curIdx;
			curIdx++;
			
			// searching end of word
			if (curIdx < size)
			{
				eos = (text.charAt(startIdx) == '\"') ? '\"' : ' ';
				for (; curIdx < size; curIdx++) if (text.charAt(curIdx) == eos) break;
			}	

			if (eos == '\"') result.addElement(text.substring(startIdx+1, curIdx));
			else result.addElement(text.substring(startIdx, curIdx));
			curIdx++;
			if (curIdx >= size) break;
		}
			
		String[] resultArray = new String[result.size()];
		for (int i = 0; i < result.size(); i++) resultArray[i] = (String)result.elementAt(i);
		return resultArray;
	}
	
	static void process_lang_file(String inFile, String outFile) throws BuildException
	{
		System.out.println("inFile="+inFile+", outFile="+outFile);
		String line;
		Vector data = new Vector();
		
		try
		{
			File srcFile = new File(inFile);
			InputStreamReader isr = new InputStreamReader(new FileInputStream(srcFile), "UTF-8");
			BufferedReader reader = new BufferedReader(isr);
			
			FileOutputStream ostream = new FileOutputStream(outFile);
			DataOutputStream dos = new DataOutputStream(ostream);
			
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.length() == 0) continue;
				if (line.charAt(0) == '/') continue;
				
				StringBuffer strBuf = new StringBuffer();
				int lineLen = line.length();
				for (int i = 0; i < lineLen; i++)
				{
					char chr = line.charAt(i);
					String addText = null;
					
					if ((chr == '\\') && (i != (lineLen-1)))
					{
						char chr2 = line.charAt(i+1);
						if (chr2 == 'n') addText = "\n";
						if (chr2 == 'r') addText = "\r";
						if (chr2 == 't') addText = "\t";
					}
					
					if (addText != null)
					{
						strBuf.append(addText);
						i++;
					}
					else
					{
						if (chr < ' ') chr = ' ';
						strBuf.append(chr);
					}	
				}
				
				String[] words = devideToWords(strBuf.toString());
				if (words.length < 2) throw new BuildException("Wrong lang line '"+line+"' in file '"+inFile+"'");
				data.addElement(words);
			}
			
			int size = data.size();
			dos.writeShort(size);
			for (int i = 0; i < size; i++)
			{
				String[] pair = (String[])data.elementAt(i);
				dos.writeUTF(pair[0]);
				dos.writeUTF(pair[1]);
			}
			
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}
	}

	public void execute() throws BuildException
	{
		StringTokenizer strTok = new StringTokenizer(languages, ",");
		while (strTok.hasMoreTokens())
		{
			String langName = strTok.nextToken();
			process_lang_file(inDir+"/"+langName+".lang", outDir+"/"+langName+".lng");
		}
	}

	public void setLanguages(String value)
	{
		languages = value;
	}

	public void setInDir(String value)
	{
		inDir = value;
	}

	public void setOutDir(String value)
	{
		outDir = value;
	}
}
