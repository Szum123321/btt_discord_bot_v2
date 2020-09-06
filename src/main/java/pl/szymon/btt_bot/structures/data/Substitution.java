package pl.szymon.btt_bot.structures.data;

import lombok.Value;

import java.time.LocalDate;

@Value
public class Substitution {
	int startPeriod;
	int endPeriod;

	String what;
	String info;

	LocalDate date;
}
