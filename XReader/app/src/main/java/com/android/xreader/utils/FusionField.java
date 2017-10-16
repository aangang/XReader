package com.android.xreader.utils;

import android.app.Activity;
import android.graphics.Bitmap;

import java.util.HashMap;

public class FusionField
{
	public static Activity baseActivity = null;
	public static float widthScale = 0;
	public static float HeightScale = 0;
	public static float density = 0;
	public static HashMap<String, Bitmap> imageMap = new HashMap<String, Bitmap>();
}