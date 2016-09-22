package mlautobot;

public interface FeatureCallback {

	public void features(byte[] features, int width, int height, float[] accelerometerFeatures);
}
