package langs;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class LangsTask extends Task
{
	private String languages, inDir, outDir, srcDir;
	private static Hashtable shortKeys = new Hashtable();
	
	private static char[] shortKeyElems = 
	{
		'_', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
		'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
		'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
		'W', 'X', 'Y', 'Z',
	};
	
	static private int shortKeywordCounter = 0;
	
	static boolean isSpecialKey(String keyword)
	{
		return ((keyword.indexOf("error_") == 0) || (keyword.indexOf("lang_") == 0)); 
	}
	
	static String[] readFile(String fileName) throws Exception
	{
		Vector lines = new Vector();
		String line;
		
		InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
		BufferedReader reader = new BufferedReader(isr);
		while ((line = reader.readLine()) != null) lines.add(line);
		reader.close();
		isr.close();
		
		String[] result = new String[lines.size()];
		lines.copyInto(result);
		return result;
	}
	
	static String keyToShortKey(String keyword)
	{
		if ( isSpecialKey(keyword) ) return keyword;
		
		String tableKeyword = (String)shortKeys.get(keyword);
		if (tableKeyword != null) return tableKeyword;
		
		StringBuffer shortKeyword = new StringBuffer();
	
		int value = shortKeywordCounter;
		do
		{
			shortKeyword.append(shortKeyElems[value%shortKeyElems.length]);
			value /= shortKeyElems.length;
		} while (value > 0);
		
		String newKey = shortKeyword.toString(); 
		
		shortKeys.put(keyword, newKey);
		
		shortKeywordCounter++;
		
		return newKey;
	}

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
	
	static private Vector readLangFile(String inFile)
	{
		Vector data = new Vector();
		String line;
		
		try
		{
			File srcFile = new File(inFile);
			InputStreamReader isr = new InputStreamReader(new FileInputStream(srcFile), "UTF-8");
			BufferedReader reader = new BufferedReader(isr);
	
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
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}
		return data;
	}
	
	static private void writeLngFile(Vector pairs, String outFile, boolean onelang) throws BuildException
	{
		try
		{
			FileOutputStream ostream = new FileOutputStream(outFile);
			DataOutputStream dos = new DataOutputStream(ostream);
			
			int size = pairs.size();
			dos.writeShort(size);
			for (int i = 0; i < size; i++)
			{
				String[] pair = (String[])pairs.elementAt(i);
				dos.writeUTF(keyToShortKey(pair[0]));
				dos.writeUTF(pair[1]);
			}
			dos.flush();
			ostream.close();
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}
	}
	
	  // Scans the given directory (srcDir/srcDirExt) for Java source files
	static private String[] scanDir(File srcDir, String srcDirExt)
	{
		// Initalize vector
		Vector filenames = new Vector();

		// Get all Java source file in the current directory
		File[] files = (new File(srcDir, srcDirExt)).listFiles();
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isFile() && (files[i].getName().endsWith(".java")))
			{
				filenames.add(srcDir + srcDirExt + File.separator + files[i].getName());
			}
			else if (files[i].isDirectory())
			{
				filenames.addAll(Arrays.asList(scanDir(srcDir, srcDirExt + File.separator + files[i].getName())));
			}
		}

		// Return Vector as array
		String[] ret = new String[filenames.size()];
		filenames.copyInto(ret);
		return (ret);

	}
	
	static private String replaceString(String str, String from, String to)
	{
		for (;;)
		{
			int index = str.indexOf(from); 
			if (index == -1) break;
			str = str.substring(0, index)+to+str.substring(index+from.length(), str.length());
		}
		return str;
	}

	static private void replaceLangKeysInSources(String fileName)
	{
		String line;
		
		try
		{
			// load source file
			line = readAndPrepareSrcFile(fileName);
			
			// replase old keys to new ones
			Enumeration allKeys = shortKeys.keys();
			while (allKeys.hasMoreElements())
			{
				String oldKey = (String)allKeys.nextElement();
				String newKey = (String)shortKeys.get(oldKey);
				line = replaceString(line, "\""+oldKey+"\"", "\""+newKey+"\"");
			}
			
			// Write source lines with new lang keys 
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
			BufferedWriter writer = new BufferedWriter(osw);
			writer.write(line);
			writer.newLine();
			writer.close();
			osw.close();
			
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}
	}
	
	static void generateStringSrcFile(String fileName)
	{
		String[] lines;
		
		try
		{
			lines = readFile(fileName);
			
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
			BufferedWriter writer = new BufferedWriter(osw);
			for (int i = 0; i < lines.length; i++)
			{
				writer.write(lines[i]);
				writer.newLine();
			}
			writer.close();
			osw.close();
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}
	}
	
	// Load and prepare file (remove comments)
	private final static int PP_STATE_DIRECT_COPY       = 1;
	private final static int PP_STATE_WAIT_FOR_COMMENT  = 2;
	private final static int PP_STATE_SIMPLE_COMMENT    = 3;
	private final static int PP_STATE_MULTILINE_COMMENT = 4;
	private final static int PP_STATE_WAIT_FOR_MLC_END  = 5;
	private final static int PP_STATE_STRING            = 6;
	
	static String readAndPrepareSrcFile(String fileName) throws BuildException
	{
		StringBuffer buffer = new StringBuffer();
		int state = PP_STATE_DIRECT_COPY;
		
		try
		{
			InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName));
			BufferedReader reader = new BufferedReader(isr);
			
			for (;;)
			{
				int curByte = reader.read();
				if (curByte == -1) break;
				
				switch (state)
				{
				case PP_STATE_DIRECT_COPY:
					if (curByte == '/') state = PP_STATE_WAIT_FOR_COMMENT;
					else if (curByte == '\"')
					{
						state = PP_STATE_STRING;
						buffer.append((char)curByte);
					}
					else /*if (curByte >= ' ')*/  buffer.append((char)curByte);
					break;
					
				case PP_STATE_WAIT_FOR_COMMENT:
					if (curByte == '*') state = PP_STATE_MULTILINE_COMMENT;
					else if (curByte == '/') state = PP_STATE_SIMPLE_COMMENT;
					else
					{
						buffer.append('/');
						buffer.append((char)curByte);
						state = PP_STATE_DIRECT_COPY;
					}
					break;
					
				case PP_STATE_SIMPLE_COMMENT:
					if ((curByte == '\r') || (curByte == '\n')) state = PP_STATE_DIRECT_COPY;
					break;
					
				case PP_STATE_MULTILINE_COMMENT:
					if (curByte == '*') state = PP_STATE_WAIT_FOR_MLC_END;
					break;
					
				case PP_STATE_WAIT_FOR_MLC_END:
					if (curByte == '/') state = PP_STATE_DIRECT_COPY;
					else if (curByte == '*') state = PP_STATE_WAIT_FOR_MLC_END;
					else state = PP_STATE_MULTILINE_COMMENT;
					break;
					
				case PP_STATE_STRING:
					buffer.append((char)curByte);
					if (curByte == '\"') state = PP_STATE_DIRECT_COPY;
					break;
				}
				
				
			}
			
			reader.close();
			isr.close();
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}
		return buffer.toString();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
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
	
	public void setSrcDir(String value)
	{
		srcDir = value;
	}
	
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	public void execute() throws BuildException
	{
		System.out.println("Creating lng file[s]... ");
		shortKeys.clear();
		Vector langs = new Vector(); 
		StringTokenizer strTok = new StringTokenizer(languages, ",");
		while (strTok.hasMoreTokens())
		{
			String langName = strTok.nextToken();
			langs.add(langName);
		}
		
		int size = langs.size();
		if (size == 0) new BuildException("No language specified");
		Vector pairs;
		for (int i = 0; i < size; i++)
		{
			String langName = (String)langs.elementAt(i);
			pairs = readLangFile(inDir+"/"+langName+".lang");
			writeLngFile(pairs, outDir+"/"+langName+".lng", size == 1);
		}
		
		try
		{
			FileOutputStream ostream = new FileOutputStream(outDir+"/langlist.lng");
			DataOutputStream dos = new DataOutputStream(ostream);
			
			dos.writeShort(langs.size());
			for (int i = 0; i < langs.size(); i++)
			{
				dos.writeUTF((String)langs.elementAt(i));
			}
			
			dos.flush();
			ostream.close();
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}

		// Replace keys in sources
		System.out.println("Preprocessing java sources... ");
		String[] files = scanDir(new File(srcDir), "");
		
		for (int i = 0; i < files.length; i++)
		{
			replaceLangKeysInSources(files[i]);
		}
	}
	
}
