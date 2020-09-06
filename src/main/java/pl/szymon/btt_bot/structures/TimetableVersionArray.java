package pl.szymon.btt_bot.structures;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import java.util.List;

@Value
public class TimetableVersionArray {
	@SerializedName("default_num")
	int defaultNum;

	List<TimetableVersion> timetables;
}
