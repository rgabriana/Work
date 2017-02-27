package com.emscloud.ws;

import java.text.ParseException;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.emscloud.model.EmState;
import com.emscloud.model.EmStateList;
import com.emscloud.model.EmTasksList;
import com.emscloud.service.EmStateManager;
import com.emscloud.util.UTCConverter;


@Controller
@Path("/org/emstate")
public class EmStateService {
	
	@Resource
	EmStateManager emStateManager;

	
	@Path("getEmStateListByEmInstanceId/{emInstanceId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmStateList getEmStateListByEmInstanceId(
    		@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway,@PathParam("emInstanceId") long emInstanceId ) throws ParseException {
    	
    	
		EmStateList oEmStateList = emStateManager.getLatestEmStateListByEmInstanceId(orderway, (page - 1) * EmStateList.DEFAULT_ROWS, EmStateList.DEFAULT_ROWS,emInstanceId);
		oEmStateList.setPage(page);
		List<EmState> emStateList = oEmStateList.getEmState();
		
		if(emStateList !=null && !emStateList.isEmpty()){
			for(EmState emState :emStateList){
				emState.setUtcSetTime(UTCConverter.getUTCTime(emState.getSetTime()));
			}
		}
		else {
			emStateList = new ArrayList<EmState>();
		}
		
		oEmStateList.setEmState(emStateList);
		return oEmStateList;
    }
}
