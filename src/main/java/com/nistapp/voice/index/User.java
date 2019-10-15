package com.nistapp.voice.index;

import com.nistapp.voice.index.models.Userclicknodes;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;

@Path("/user")
public class User {

	@Inject
	EntityManager em;

	static String getAlphaNumericString(int n)
	{

		// chose a Character random from this String
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
				+ "0123456789"
				+ "abcdefghijklmnopqrstuvxyz";

		// create StringBuffer size of AlphaNumericString
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {

			// generate a random number between
			// 0 to AlphaNumericString variable length
			int index
					= (int)(AlphaNumericString.length()
					* Math.random());

			// add Character one by one in end of sb
			sb.append(AlphaNumericString
					.charAt(index));
		}

		return sb.toString();
	}

	@GET
	@Path("/getsessionkey")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getsessionkey(@CookieParam("nist-voice-usersessionid") Cookie sessioncookie) {
		if (sessioncookie == null) {
			String sessionkey = getAlphaNumericString(20);
			Date dt = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(dt);
			c.add(Calendar.DATE, 365);
			dt = c.getTime();
//			NewCookie newCookie = new NewCookie("nist-voice-usersessionid","");
//			NewCookie newCookie1 = new NewCookie("nist-voice-usersessionid", sessionkey, 365*24*60*60, dt, false, true);
			NewCookie newCookie1 = new NewCookie("nist-voice-usersessionid", sessionkey,"/", "", "Nistapp voice user session", 365*24*60*60, false);
			return Response.ok(sessionkey).cookie(newCookie1).build();
		} else {
			return Response.ok(sessioncookie.getValue()).build();
		}
	}

	@POST
	@Path("/clickednode")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Userclicknodes Userclickednode(Userclicknodes userclicknodes){
		em.persist(userclicknodes);
		em.flush();
		return userclicknodes;
	}

}
