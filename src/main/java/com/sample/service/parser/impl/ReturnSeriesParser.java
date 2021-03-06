package com.sample.service.parser.impl;

import static com.sample.util.Util.getDate;
import static com.sample.util.Util.isNumeric;
import static com.sample.util.Util.nonNull;
import static com.sample.util.Util.stringNotBlank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sample.domain.Domain;
import com.sample.domain.ReturnSeries;
import com.sample.service.parser.CSVFileParser;

/**
 * Parser to process RerturnSeries data in CSV files.
 * 
 * @author Aravind
 *
 */
@Service("RETURNSERIES")
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReturnSeriesParser extends AbstractParser implements CSVFileParser {

	private Function<CSVRecord, ReturnSeries> populateReturnSeriesFn = record -> {
		ReturnSeries returnSeries = new ReturnSeries();
		String code = record.get(0);
		String strDate = record.get(1);
		String strPercent = record.get(2);

		returnSeries.setCode(code);
		Date date = getDate(strDate);
		returnSeries.setDate(date);

		if (isNumeric(strPercent)) {
			returnSeries.setReturnPercent(new BigDecimal(strPercent));
		}
		returnSeries.setDate(date);

		return returnSeries;
	};

	@Override
	public List<? extends Domain> process(List<CSVRecord> records) throws Exception {

		List<ReturnSeries> returns = records.stream().map(populateReturnSeriesFn)
				.filter(r -> stringNotBlank.test(r.getCode())).filter(r -> nonNull.test(r.getDate()))
				.filter(r -> nonNull.test(r.getReturnPercent())).collect(Collectors.toList());

		return returns;
	}

	@Override
	public boolean insert(List<? extends Domain> records) throws Exception {
		@SuppressWarnings("unchecked")
		List<ReturnSeries> returns = (List<ReturnSeries>) records;

		if (returns == null || returns.size() == 0) {
			return false;
		}

		int insertCount = fundDao.insertReturnSeries(returns);

		return insertCount == returns.size();
	}

}
