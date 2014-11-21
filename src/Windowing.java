import java.lang.reflect.Array;

class Windowing {

    private String wtype = "None";

	public void setWindowType(String s) {
		wtype = s;
	}

    public float[] apply(float[] values) {

		int nSamples;
		nSamples = Array.getLength (values);
        float[] windowedValues = new float[nSamples];

		if (wtype.equals("Rectangular")) { // rectangular wtype
			for (int i = 0 ; i < nSamples; i++)
			{
				// do nothing
				windowedValues[i] = values[i];
			}
		}
		
		if (wtype.equals("Welch")) {
			for (int i = 0 ; i < nSamples; i++)
			{
				float f;
				f = (i-nSamples/2.0f)/(nSamples/2.0f);
				windowedValues[i] = values[i] * (1.0f-f*f);
			}
		}
		
		if (wtype.equals("Bartlett")) {
			int i, j;
			for (j = 0-nSamples/2 ; j < nSamples/2; j++)
			{
				i = j+nSamples/2;
				if (j >= 0 && j <= nSamples/2) {
					windowedValues[i] = values[i] * (1.0f-2.0f*j/nSamples);
				}
				else if (j >= 0-nSamples/2 && j < 0) {
					windowedValues[i] = values[i] * (1.0f+2.0f*j/nSamples);
				}
				else
					windowedValues[i] = values[i] * 0f;
			}		
		}
		
		if (wtype.equals("Hanning")) {
			int i, j;
			for (j = 0-nSamples/2 ; j < nSamples/2; j++)
			{
				i = j+nSamples/2;
				windowedValues[i] = values[i] * (0.5f + 0.5f * (float) Math.cos(2.0f * (float) Math.PI * j / nSamples));
			}		
		}
		if (wtype.equals("Hamming")) {
			int i, j;
			for (j = 0-nSamples/2 ; j < nSamples/2; j++)
			{
				i = j+nSamples/2;
				windowedValues[i] = values[i] * (0.54f + 0.46f * (float) Math.cos(2.0f * (float) Math.PI * j / nSamples));
			}		
		}
				
        return windowedValues;
    }
}
