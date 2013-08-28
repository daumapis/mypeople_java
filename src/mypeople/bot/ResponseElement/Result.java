/**
 * 	xml과 json을 객체로 받아오기 위한 클래스
 */

package mypeople.bot.ResponseElement;

public class Result {
	private int code; 
	private String message;
	private Buddys buddys;
	
	Result() {}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public Buddys getBuddys() {
		return buddys;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setBuddys(Buddys buddys) {
		this.buddys = buddys;
	}
}
