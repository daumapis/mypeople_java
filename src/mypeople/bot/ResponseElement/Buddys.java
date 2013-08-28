/**
 * 	xml과 json을 객체로 받아오기 위한 클래스
 */

package mypeople.bot.ResponseElement;

public class Buddys {
	private String buddyId;
	private String name;
	private String photoId;
	
	public Buddys() {}

	public String getBuddyId() {
		return buddyId;
	}

	public String getName() {
		return name;
	}

	public String getPhotoId() {
		return photoId;
	}

	public void setBuddyId(String buddyId) {
		this.buddyId = buddyId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
}
