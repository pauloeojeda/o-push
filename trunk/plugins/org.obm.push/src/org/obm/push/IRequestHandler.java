package org.obm.push;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IRequestHandler {

	public void process(HttpServletRequest req, HttpServletResponse resp);

}
