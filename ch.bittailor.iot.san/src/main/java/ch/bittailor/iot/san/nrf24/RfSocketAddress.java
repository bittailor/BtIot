package ch.bittailor.iot.san.nrf24;

public class RfSocketAddress {
	private final int mId;
	private final int mLevel;
	
	RfSocketAddress(int id) {
		mId = id;
		mLevel = calculateLevel(mId);
	}
	
	public int getId() {
		return mId;
	}

	public int getLevel() {
		return mLevel;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RfSocketAddress other = (RfSocketAddress) obj;
		if (mId != other.mId)
			return false;
		return true;
	}

	private static int calculateLevel(int iId) {
	   if (iId <   1) return 0;
	   if (iId <   6) return 1;
	   if (iId <  31) return 2;
	   if (iId < 156) return 3;
	   return 4;
	}
	
}
