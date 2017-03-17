/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) [2017] [velli20]
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package com.velli.tachograph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnTaskCompleted;

import android.content.Context;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExportEvents implements OnTaskCompleted {
	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	private File file;
	private OnFileSavedListener listener;
	private Context mContext;
	private boolean mIncludeAutomaticallyCalculatedRestingEvents = false;
	
	public interface OnFileSavedListener {
		void onFileSaved(File file);
	}
	

	public void setFile(File file){
		this.file = file;
	}

	public void setIncludeAutomaticallyCalculatedRestingEvents(boolean include) {
		mIncludeAutomaticallyCalculatedRestingEvents = include;
	}
	
	public void write(Context context) {
		mContext = context;
        DataBaseHandler db = DataBaseHandler.getInstance();
        db.getAllEvents(this, false, false, mIncludeAutomaticallyCalculatedRestingEvents);
	}
	
	public void write(Context context, int[] rowIds) {
		mContext = context;
        DataBaseHandler db = DataBaseHandler.getInstance();
        db.getEventsByRowIds(this, rowIds, mIncludeAutomaticallyCalculatedRestingEvents);
	}
	
	public void setOnFileSavedListener(OnFileSavedListener listener){
		this.listener = listener;
	}

	private void createLabel(WritableSheet sheet) throws WriteException {
		// Lets create a times font
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		// Define the cell format
		times = new WritableCellFormat(times10pt);
		// Lets automatically wrap the cells
		times.setWrap(true);

		// create create a bold font with unterlines
		WritableFont times10ptBoldUnderline = new WritableFont( WritableFont.TIMES, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		// Lets automatically wrap the cells
		timesBoldUnderline.setWrap(true);

		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);

		// Write a few headers
		addCaption(sheet, 0, 0, mContext.getString(R.string.export_events_start_date));
		addCaption(sheet, 1, 0, mContext.getString(R.string.export_events_start_time));
		addCaption(sheet, 2, 0, mContext.getString(R.string.export_events_end_date));
		addCaption(sheet, 3, 0, mContext.getString(R.string.export_events_end_time));
		addCaption(sheet, 4, 0, mContext.getString(R.string.export_events_duration));
		addCaption(sheet, 5, 0, mContext.getString(R.string.export_events_event_type));
		addCaption(sheet, 6, 0, mContext.getString(R.string.export_events_mileage_at_start));
		addCaption(sheet, 7, 0, mContext.getString(R.string.export_events_mileage_at_end));
		addCaption(sheet, 8, 0, mContext.getString(R.string.export_events_note));

	}


	
	private void writeEvents(ArrayList<Event> list) throws IOException, WriteException {
	    final String[] events = mContext.getResources().getStringArray(R.array.event_explanations);
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(new Locale("en", "EN"));
		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet("Log", 0);
		WritableSheet excelSheet = workbook.getSheet(0);
		createLabel(excelSheet);

	    int listSize = list.size();
	    
		for(int i = 0; i < listSize; i++){
			Event ev = list.get(i);
			addLabel(excelSheet, 0, i+1, DateCalculations.createShortDateTimeString(ev.getStartDateInMillis()));
			addLabel(excelSheet, 1, i+1, DateCalculations.createTimeString(ev.getStartDateInMillis()));
			addLabel(excelSheet, 2, i+1, DateCalculations.createShortDateTimeString(ev.getEndDateInMillis()));
			addLabel(excelSheet, 3, i+1, DateCalculations.createTimeString(ev.getEndDateInMillis()));
			addLabel(excelSheet, 4, i+1, DateCalculations.convertDatesInHours(ev.getStartDateInMillis(), ev.getEndDateInMillis()));
			addLabel(excelSheet, 5, i+1, events[ev.getEventType()]);
			addLabel(excelSheet, 6, i+1, String.valueOf(ev.getMileageStart()));
			addLabel(excelSheet, 7, i+1, String.valueOf(ev.getMileageEnd()));
			addLabel(excelSheet, 8, i+1, ev.getNote() == null ? "-" : ev.getNote());
		}
		
		if(listener != null){
			listener.onFileSaved(file);
		}
		workbook.write();
		workbook.close();
		
		mContext = null;
		listener = null;
	}
	
	@Override
	public void onTaskCompleted(ArrayList<Event> list) {
		try {
			writeEvents(list);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void addCaption(WritableSheet sheet, int column, int row, String s) throws WriteException {
		Label label;
		label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);
	}

	private void addLabel(WritableSheet sheet, int column, int row, String s) throws WriteException {
		Label label;
		label = new Label(column, row, s, times);
		sheet.addCell(label);
	}

	

	
}
