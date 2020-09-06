package pl.szymon.btt_bot.structures;

import com.google.gson.annotations.SerializedName;
import lombok.*;
import lombok.experimental.NonFinal;

import java.time.LocalDate;

@Value
public class TimetableVersion {
	int tt_num;
	int year;
	String text;
	boolean hidden;

	@SerializedName("datefrom")
	LocalDate dateFrom;

	@NonFinal
	LocalDate dateTo;

	public LocalDate getDateTo() {
		if(dateTo == null)
			updateDateTo();

		return dateTo;
	}

	public void updateDateTo() {
		dateTo = dateFrom.plusDays(6);
	}
}