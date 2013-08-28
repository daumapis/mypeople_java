/**
 * 마이피플 봇 API JAVA 샘플 코드입니다.
 * 
 * 알림콜백을 받은 뒤 action 값에 따라 처리하는 방식입니다.
 * 
 * 
 * 테스트 환경
 * Tomcat version 6.0.35
 * Servlet version 2.5
 * JSP version 2.1
 * JDK version 1.6.0
 * 
 * 사용한 라이브러리
 * XStream version 1.4.4
 * Gson version 2.2.4
 * 
 * @author 강현구(khg1031@hanmail.net)
 * 
 */

package mypeople.bot.java;

import java.io.*;
import java.net.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mypeople.bot.ResponseElement.*;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;

public class Callback extends HttpServlet {
	private static final String API_URL_PREFIX = "https://apis.daum.net";
	private static final String MYPEOPLE_BOT_APIKEY = "[API KEY를 입력하세요]"; // 봇을 등록하고 받은 API KEY를 입력하세요.
	private static final String API_URL_POSTFIX = "&apikey=" + MYPEOPLE_BOT_APIKEY;

	private enum actionList {
		addBuddy, sendFromMessage, createGroup, 
		inviteToGroup, exitFromGroup, sendFromGroup;
	}
	
	private String action;
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException { }
    
    // 모든 action은 POST로 처리합니다.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		this.action = request.getParameter("action");
		
    	selectAction(this.action, request);
	}
    
    public void selectAction(String action, HttpServletRequest request) {
    	/*
    	 *  Java6에서는 switch문에 문자열 비교를 지원하지 않습니다.
    	 */
    	
    	switch( actionList.valueOf(action) ) {
    	case addBuddy :
    		// 봇을 친구로 등록한 사용자의 이름을 가져와 환영 메시지를 보냅니다.
    		greetingMessageToBuddy(request);
    		break;
    		
    	case sendFromMessage :
    		// 말을 그대로 따라합니다.
    		echoMessageToBuddy(request);
    		break;
    		
    	case createGroup :
    		// 그룹대화방이 생성되었을때 그룹대화를 만든 사람과 대화에 참여한 친구들의 이름을 출력합니다.
    		groupCreatedMessage(request);
    		break;
    		
    	case inviteToGroup :
    		// 그룹대화방에 친구가 새로 추가될 경우 누가 누구를 초대했는지 출력합니다.
    		groupGreetingMessage(request);
    		break; 
    		
    	case exitFromGroup :
    		// 그룹대화방에서 친구가 나갔을 경우 정보를 출력합니다.
    		groupExitAlertMessage(request);
    		break;
    		
    	case sendFromGroup :
    		// 그룹 대화방에서 특정 메시지가 왔을때 반응합니다.
    		filterGroupMessage(request);
    		break;
    		
    	default:
			break;
    	}
    }
    
    public void greetingMessageToBuddy(HttpServletRequest request)
    {
    	String buddyId = request.getParameter("buddyId");	// 봇을 친구 추가한 친구ID
		String msg = getBuddyName(buddyId) + "님 안녕하세요!";
		
		sendMessage("buddy", buddyId, msg);
    }
    
    public void echoMessageToBuddy(HttpServletRequest request)
    {
    	String buddyId = request.getParameter("buddyId");	// 메시지를 보낸 친구ID
    	String msg = request.getParameter("content");	// 메시지 내용
    	
    	sendMessage("buddy", buddyId, msg);
    }
    
    public void groupCreatedMessage(HttpServletRequest request)
    {
    	String buddyId = request.getParameter("buddyId");	// 그룹 대화를 만든 친구ID
    	String content = request.getParameter("content");	// 그룹 대화방 친구 목록(json 형태)
    	String groupId = request.getParameter("groupId");	// 그룹ID
    	
    	// json을 처리하기 위한 객체 생성
    	Gson gson = new Gson();
    	// gson을 사용해서 json을 배열로 받습니다.
		Buddys[] buddys = gson.fromJson(content, Buddys[].class);
		String buddysName = "";
		
		for(int i=0; i<buddys.length; i++) {
			buddysName += " " + buddys[i].getName();
		}
		// 그룹에 생성메시지 보내기
		String msg = getBuddyName(buddyId) + "님이 새로운 그룹 대화를 만들었습니다. 그룹 멤버는 " + buddysName + " 입니다.";
		sendMessage("group", groupId, msg);
    }
    
    public void groupGreetingMessage(HttpServletRequest request)
    {
    	String buddyId = request.getParameter("buddyId");	// 그룹 대화방에 초대한 친구ID
    	String content = request.getParameter("content");	// 그룹 대화방에 초대된 친구 정보(json 형태)
    	String groupId = request.getParameter("groupId");	// 그룹ID
    	
    	// json을 처리하기 위한 객체 생성
    	Gson gson = new Gson();
    	// gson을 사용해서 json을 배열로 받습니다.
		Buddys[] buddys = gson.fromJson(content, Buddys[].class);
		String buddysName = "";
		
		for(int i=0; i<buddys.length; i++) {
			buddysName += " " + buddys[i].getName();
		}
		// 그룹에 환영 메시지 보내기
		String msg = getBuddyName(buddyId) + "님께서 " + buddysName + "님을 초대하였습니다.";
		sendMessage("group", groupId, msg);
    }
    
    public void groupExitAlertMessage(HttpServletRequest request)
    {
    	String buddyId = request.getParameter("buddyId");	// 그룹 대화방을 나간 친구ID
    	String groupId = request.getParameter("groupId");	// 그룹 대화방ID
    	
    	// 그룹에 퇴장 알림 메시지 보내기
    	String msg = "슬프게도..." + getBuddyName(buddyId) + "님께서 우리를 떠나갔어요.";
    	sendMessage("group", groupId, msg);
    }
    
    public void filterGroupMessage(HttpServletRequest request)
    {
    	String groupId = request.getParameter("groupId");	// 그룹 대화방ID
    	String buddyId = request.getParameter("buddyId");	// 그룹 대화방에서 메시지를 보낸 친구ID
    	String content = request.getParameter("content");	// 메시지 내용
    	
    	// "마이피플"이라는 단어가 포함된 메시지가 나오면 반응
    	if(content.indexOf("마이피플") > -1) {
    		String msg = getBuddyName(buddyId) + "님, 역시 마이피플이 짱이죠?";
    		sendMessage("group", groupId, msg);
    	}
    	
    	// 퇴장처리
    	if(content.equals("퇴장") || content.equals("exit")) {
    		exitGroup(groupId);
    	}
    }
    
    public void exitGroup(String groupId)
    {
    	String address = API_URL_PREFIX + "/mypeople/group/exit.xml?groupId=" + groupId + API_URL_POSTFIX;
    	try {
			URL url = new URL(address);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
    		
    		conn = SSL_VERIFYPEER(conn);
    		
    		conn.setDoOutput(true);
    		
			// 바이트기반 스트림을 문자기반 스트림으로 연결시켜준다.
			// 바이트기반 스트림의 데이터를 지정된 인코딩의 문자데이터로 변환하는 작업을 수행한다.
			OutputStreamWriter ows = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			ows.write(address);
			ows.flush();
			
			ows.close();
		} catch (Exception e) {
			e.getMessage();
		}
    }

    public void sendMessage(String target, String targetId, String msg)
    {
    	try {
        	// 메시지 전송 url 지정
        	String address =	API_URL_PREFIX + "/mypeople/" + target + "/send.xml?apikey=" + MYPEOPLE_BOT_APIKEY;
    		URL url = new URL(address);
    		HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
    		
    		conn = SSL_VERIFYPEER(conn);
    		
	    	// CR처리. \n이 있을경우 에러남
			msg = msg.replace("\n", "\r").replace('\n',  '\r');
			
			// 파라미터 설정
			address += "&" + target + "Id=";
			address += targetId;
			
			address += "&content=";
			address += msg;
			
			// output true
			conn.setDoOutput(true);
			
			// 바이트기반 스트림을 문자기반 스트림으로 연결시켜준다.
			// 바이트기반 스트림의 데이터를 지정된 인코딩의 문자데이터로 변환하는 작업을 수행한다.
			OutputStreamWriter ows = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			ows.write(address);
			ows.flush();
			
			// br로 결과를 받아온다.
			InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			
			ows.close();
		} catch (Exception e) {
			e.getMessage();
		}
    }
    
    public String getBuddyName(String buddyId)
    {
    	try {
    		// 프로필 정보보기 url 지정
    		String address =	API_URL_PREFIX + "/mypeople/profile/buddy.xml?buddyId=" + buddyId + API_URL_POSTFIX;
    		URL url = new URL(address);
    		HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
    		
    		conn = SSL_VERIFYPEER(conn);
    		
			// 쓰기, 읽기 모드 지정
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			// 서버로 연결하여 url 정보를 보냅니다.
			OutputStreamWriter ows = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			ows.write(address);
			ows.flush();
			ows.close();

			// 응답 받기
			InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			
			// 응답 받은 xml을 처리하기 위한 XStream 객체 생성
			XStream xstream = new XStream();
			
			// 사용할 클래스 지정
			xstream.alias("result", Result.class);
			xstream.alias("buddys", Buddys.class);
			
			// 전송 받은 xml에서 result 객체로 저장
			Result result = (Result)xstream.fromXML(br);

			return result.getBuddys().getName();
    	} catch (Exception e) {
			return e.getMessage();
		}
    }
    
    // SSL verify
    private HttpsURLConnection SSL_VERIFYPEER(HttpsURLConnection conn) {
		conn.setHostnameVerifier(new HostnameVerifier()
		{
			public boolean verify(String hostname, SSLSession session)
			{
				return true;
			}
		});
		return conn;
    }
}