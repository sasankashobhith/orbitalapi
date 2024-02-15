package com.bundee.msfw.servicefw.srvutils.job;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class JobReccurence {
	// Try and set the DEF value to 3AM CST which is 8AM GMT
	private static final Integer DEF_HOUR_OF_DAY = 8;

	private static final String PERIODICITY_IN_WEEKS = "w";
	private static final String PERIODICITY_IN_DAYS = "d";
	private static final String PERIODICITY_IN_HOURS = "h";
	private static final String PERIODICITY_IN_MINUTES = "m";
	private static final String PERIODICITY_IN_SECONDS = "s";

	private static final List<Integer> daysOfWeekCal = new ArrayList<Integer>(
			Arrays.asList(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
					Calendar.FRIDAY, Calendar.SATURDAY));
	private static final List<String> daysOfWeek = new ArrayList<String>(
			Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "NONE"));
	private Long recTS = -1L;
	private Integer hourOfDay = DEF_HOUR_OF_DAY;
	private Integer dayOfWeek = -1;
	private String periodicity = "";

	JobReccurence(String tvalue) throws BExceptions {
		parse(tvalue);
	}

	public Long getRecTS() {
		return recTS;
	}

	public Integer getHourOfDay() {
		return hourOfDay;
	}

	public Integer getDayOfWeek() {
		return (dayOfWeek >= 0 ? daysOfWeekCal.get(dayOfWeek) : -1);
	}

	public void setRecTS(long recTS) {
		this.recTS = recTS;
	}

	public boolean isWeekly() {
		return periodicity.equalsIgnoreCase(PERIODICITY_IN_WEEKS);
	}

	public boolean isDaily() {
		return periodicity.equalsIgnoreCase(PERIODICITY_IN_DAYS);
	}

	public long getInitialDelayMS(Calendar fromTime) {
		long computedDelayMS = 0;
		Calendar resTime = Calendar.getInstance();
		resTime.setTimeInMillis(fromTime.getTimeInMillis());

		resTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
		resTime.set(Calendar.MINUTE, 0);
		resTime.set(Calendar.SECOND, 0);
		resTime.set(Calendar.MILLISECOND, 0);

		int days2Add = 0;
		if (isWeekly()) {
			int todayDOW = fromTime.get(Calendar.DAY_OF_WEEK);
			int todayHOD = fromTime.get(Calendar.HOUR_OF_DAY);
			int todayDOWIdx = idxOfDOW(todayDOW);

			if (dayOfWeek >= 0) {
				if (dayOfWeek > todayDOWIdx) {
					days2Add = dayOfWeek - todayDOWIdx;
				} else if (dayOfWeek < todayDOWIdx) {
					days2Add = 7 - (todayDOWIdx - dayOfWeek);
				} else if (dayOfWeek == todayDOWIdx) {
					if (todayHOD > hourOfDay) {
						days2Add = 7;
					}
				}
			}
		} else if (isDaily()) {
			if (resTime.getTimeInMillis() < fromTime.getTimeInMillis()) {
				days2Add = 1;
			}
		}

		if (days2Add > 0) {
			resTime.add(Calendar.DAY_OF_MONTH, days2Add);
			computedDelayMS = resTime.getTimeInMillis() - fromTime.getTimeInMillis();
		}
		return computedDelayMS;

	}

	private String getDoWName() {
		return (dayOfWeek < 0 ? daysOfWeek.get(daysOfWeek.size() - 1) : daysOfWeek.get(dayOfWeek));
	}

	private int idxOfDOW(int calDOW) {
		int idx = 0;
		for (; idx < daysOfWeekCal.size(); idx++) {
			if (daysOfWeekCal.get(idx) == calDOW) {
				break;
			}
		}
		return idx;
	}

	// Format is expected to be <recurrence_value>|<hour_of_day>|<day_of_week>
	private void parse(String reccurenceStr) throws BExceptions {
		if (reccurenceStr == null || reccurenceStr.isBlank())
			return;
		String[] vals = reccurenceStr.split(",");
		if (vals != null) {
			if (vals.length >= 1) {
				parseRecTS(vals[0]);
			}
			if (vals.length >= 2) {
				hourOfDay = parseIntValue("HourOfDay", vals[1]);
			}
			if (vals.length >= 3) {
				dayOfWeek = parseIntValue("DayOfWeek", vals[2]);
			}

			if (recTS <= 0) {
				throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, recTS + " is invalud value!");
			}
			if (hourOfDay >= 0 && hourOfDay > 23) {
				throw new BExceptions(FwConstants.PCodes.INVALID_VALUE,
						hourOfDay + " hourOfDay should be between 0-23!");
			}
			if (dayOfWeek >= 0 && dayOfWeek > 6) {
				throw new BExceptions(FwConstants.PCodes.INVALID_VALUE,
						dayOfWeek + " dayOfWeek should be between 0-6!");
			}
		}
	}

	private void parseRecTS(String tvalue) throws BExceptions {
		if (tvalue == null || tvalue.length() < 2)
			return;

		periodicity = tvalue.substring(tvalue.length() - 1, tvalue.length()).toLowerCase();
		String timeVal = tvalue.substring(0, tvalue.length() - 1);
		long longVal = -1L;

		try {
			longVal = Long.parseLong(timeVal);
		} catch (NumberFormatException ex) {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "RecTS value is incorrect " + tvalue);
		}

		switch (periodicity) {
		case PERIODICITY_IN_WEEKS:
			recTS = longVal * 7 * 24 * 60 * 60;
			break;
		case PERIODICITY_IN_DAYS:
			recTS = longVal * 24 * 60 * 60;
			break;
		case PERIODICITY_IN_HOURS:
			recTS = longVal * 60 * 60;
			break;
		case PERIODICITY_IN_MINUTES:
			recTS = longVal * 60;
			break;
		case PERIODICITY_IN_SECONDS:
			recTS = longVal;
			break;
		}
	}

	private Integer parseIntValue(String ctx, String tvalue) throws BExceptions {
		Integer value = -1;
		if (tvalue == null || tvalue.isBlank())
			return value;
		try {
			value = Integer.parseInt(tvalue);
		} catch (NumberFormatException ex) {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, ctx + " value is incorrect " + tvalue);
		}
		return value;
	}

	@Override
	public String toString() {
		return ("recTS:[" + recTS + "] hod:[" + hourOfDay + "] dow:[" + getDoWName() + "]");
	}
}
