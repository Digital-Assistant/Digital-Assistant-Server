package com.nistapp.voice.index.converters;

import com.nistapp.voice.index.models.Userclicknodes;
import com.nistapp.voice.index.repository.UserclicknodesRepository;

import javax.inject.Inject;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Converter
public class UserclicknodeConverter implements AttributeConverter<List<Userclicknodes>, String> {

	private static final String SEPARATOR = ",";

	@Inject
	private UserclicknodesRepository userclicknodesRepository;

	@Override
	public String convertToDatabaseColumn(List<Userclicknodes> userclicknodes) {
		if (userclicknodes.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (Userclicknodes userclicknode : userclicknodes)
		{
			sb.append(userclicknode.getId());
			sb.append(SEPARATOR);
		}

		return sb.toString();
	}

	@Override
	public List<Userclicknodes> convertToEntityAttribute(String userclicknodelist) {
		if (userclicknodelist == null || userclicknodelist.isEmpty()) {
			return null;
		}

		String[] pieces = userclicknodelist.split(SEPARATOR);

		if (pieces == null || pieces.length == 0) {
			return null;
		}

		List<Userclicknodes> userclicknodes = new ArrayList<>();
		for(String clickid:pieces){
			Long clicknodeid=Long.parseLong(clickid);
			Userclicknodes userclicknode = userclicknodesRepository.findById(clicknodeid);
			if(userclicknode.getId()!=null) {
				userclicknodes.add(userclicknode);
			}
		}
		return userclicknodes;
	}
}
