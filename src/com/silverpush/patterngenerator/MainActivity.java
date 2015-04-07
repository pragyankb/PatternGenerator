//*************************
package com.silverpush.patterngenerator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import javax.sound.sampled.*;


public class MainActivity {

    public static void main(String[] args) throws IOException{


        System.out.println("Enter your name here : ");
        Scanner scanIn = new Scanner(System.in);
        String userName = scanIn.nextLine();
        System.out.println("Enter number of Patterns required : ");
        int noOfPatternsRequired =  Integer.parseInt(scanIn.nextLine());
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();
        String currentDate = dateFormat.format(date).toString();


        //Change value below to generate a pattern different length. Currently 4 freq after signature
        //Change by Debasish - 24March
        //int noOfFreqInPattern = 4;
        int noOfFreqInPattern = 7;  //

        //inputFile location
        String pathFile = "./src/resources/patterns.xls";



        //Defining signature frequency for TV based audio pixel
        String SignatureFreq = "18000";

        //Allowed Freq Array (Put 19 frequency other web signature values)

        //Change by Debasish - 24 March 2015
        //String[] AllowedFreq = {"18151","18251","18351","18451","18551","18651","18751","18851","18951","19051","19151","19251","19351","19451","19551","19651","19751","19851"};
        String[] AllowedFreq = {"18150","18225","18300","18375","18450","18525","18600","18675","18750","18825","18900","18975","19050","19125","19200","19275","19350","19425","19500","19575","19650","19725","19800","19875"};
        //18225 - D,18450 -G
        //Initializing var singlepattern with signaturefreq
        String[] SinglePattern = new String[noOfFreqInPattern+1];

        //All patterns
        final ArrayList<ArrayList<String>> AllPatterns =  new ArrayList<ArrayList<String>>();
        final ArrayList<ArrayList<String>> NewPatterns =  new ArrayList<ArrayList<String>>();

        //Read all patterns to a list of array
        try{
            FileInputStream file = new FileInputStream(new File(pathFile));



            //Get the workbook instance for XLS file
            HSSFWorkbook workbook = new HSSFWorkbook(file);

            //Get first sheet from the workbook
            HSSFSheet sheet = workbook.getSheetAt(0);

            //starting with j=1 as ignoring header row
            for (int j=1; j< sheet.getLastRowNum() + 1; j++) {
                Row row = sheet.getRow(j);
                Cell cell = row.getCell(0);
                String tempstr = cell.getStringCellValue().replaceAll("[\\[\\]]", "");
                AllPatterns.add(new ArrayList<String>(Arrays.asList(tempstr.split("\\s*,\\s*"))));
            }

            for(int y=0; y< noOfPatternsRequired;y++)
            {
                //Iteration for generating Array of patternarray

                SinglePattern[0] = SignatureFreq;

                for (int i = 1; i <= noOfFreqInPattern; i++)
                {
                    String freq = AllowedFreq[(int) Math.floor(Math.random() * AllowedFreq.length)].toString();


                    //Check generated freq doesn't repeat in the elements of generatedpattern
                    if(Arrays.asList(SinglePattern).indexOf(freq)!=-1)
                    {
                        //don't count this loop iteration as this freq already exists
                        i--;
                    }
                    else
                    {
                        //append freq to the generatedpattern
                        SinglePattern[i] = freq;
                    }
                }

                if(AllPatterns.contains(new ArrayList<String>(Arrays.asList(SinglePattern))))
                {
                    y--;
                    SinglePattern = new String[SinglePattern.length];
                }
                else
                {
                    AllPatterns.add(new ArrayList<String>(Arrays.asList(SinglePattern)));
                    NewPatterns.add(new ArrayList<String>(Arrays.asList(SinglePattern)));
                    SinglePattern = new String[SinglePattern.length];
                }
            }

            int rownum = sheet.getLastRowNum() + 1;
            //Update Excel file with NewPatterns (append)
            for (ArrayList<String> l1 : NewPatterns) {
                Row row = sheet.createRow(rownum);
                Cell cell = null;
                cell = row.createCell(0);
                cell.setCellValue(l1.toString());
                cell = row.createCell(1);
                System.out.println("Enter Campaign Name");
                Scanner scan2 = new Scanner(System.in);
                String campaignName = scan2.nextLine();
                cell.setCellValue(campaignName);
                cell = row.createCell(2);
                cell.setCellValue(userName);
                cell = row.createCell(3);
                cell.setCellValue(currentDate);
                rownum++;
                System.out.println(l1.toString());

                //Creation of Audio Beacon
                GenerateAudioFile(l1.toArray(new String[l1.size()]), campaignName);

            }

            file.close();
            FileOutputStream outFile = new FileOutputStream(new File(pathFile));
            workbook.write(outFile);
            outFile.close();
            workbook.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static void GenerateAudioFile(String[] freqPattern, String campaignName) {
        int iterationFreqPattern = 2; // Total playing time 700 msecs
        int noOfFreqs = 8; //8
        double duration = 0.1; // seconds per individual freq in 8 freq pattern
        // block making total 800 msecs
        final int sampleRate = 44100;
        final int numSamples = (int) Math.floor(duration * sampleRate);
        final double sample[] = new double[numSamples];

        final byte generatedSnds[] = new byte[2 * numSamples * 8];
        // Changed by Debasish - 24th March
        //final byte generatedSnds[] = new byte[2 * numSamples * 5];

        // multiplication
        // factor is
        // present
        // to create
        // an array
        // for
        // pattern
        // of 8
        // freqs
        final byte generatedSndsFin[] = new byte[2 * numSamples * 8
                * iterationFreqPattern]; // takes iterations into account
        // loop start for 8 iterations for different freqs
        for (int j = 0; j < noOfFreqs; j++) {
            double freqOfTone = Double.parseDouble(freqPattern[j]); // hz

            final byte generatedSnd[] = new byte[2 * numSamples];

            // fill out the array
            for (int i = 0; i < numSamples; ++i) {
                // sample[i] = Math.sin(2 * Math.PI * i /
                // (sampleRate/freqOfTone));
                sample[i] = 0.65 * Math.sin((2 * Math.PI - .001) * i
                        / (sampleRate / freqOfTone));
            }

            // convert to 16 bit pcm sound array
            // assumes the sample buffer is normalised.
            int idx = 0;
            int ramp = numSamples / 20;

            for (int i = 0; i < ramp; i++) {
                // scale to maximum amplitude
                final short val = (short) ((sample[i] * 32767) * i /ramp);
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }

            for (int i = ramp; i < numSamples - ramp; i++) {
                // scale to maximum amplitude
                final short val = (short) ((sample[i] * 32767));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }

            for (int i = numSamples - ramp; i < numSamples; i++) {
                // scale to maximum amplitude
                final short val = (short) ((sample[i] * 32767)
                        * (numSamples - i) / ramp);
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }
            System.arraycopy(generatedSnd, 0, generatedSnds, j
                    * generatedSnd.length, generatedSnd.length);

        }// end of for loop
        for (int z = 0; z < iterationFreqPattern; z++) {
            System.arraycopy(generatedSnds, 0, generatedSndsFin, z
                    * generatedSnds.length, generatedSnds.length);
        }
        //Saving Audio Buffer in wav file
        saveAudioFile(campaignName,generatedSndsFin);


    }



    /**
     * Save the double array as a sound file (using .wav format).
     */
    public static void saveAudioFile(String filename, byte[] data) {
        final int SAMPLE_RATE = 44100;
        filename =  "./src/resources/" + filename + ".wav";
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        // now save the file
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            AudioInputStream ais = new AudioInputStream(bais, format, data.length);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filename));

        }
        catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

}
