/*
 * Copyright (c) 2015 Andrés Cordero
 * Web: https://github.com/Andrew67/DdrFinder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.andrew67.ddrfinder.adapters;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.andrew67.ddrfinder.R;
import com.andrew67.ddrfinder.interfaces.ApiResult;
import com.andrew67.ddrfinder.interfaces.ArcadeLocation;
import com.andrew67.ddrfinder.interfaces.MessageDisplay;
import com.andrew67.ddrfinder.interfaces.ProgressBarController;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Map;

public abstract class MapLoader extends AsyncTask<LatLngBounds, Void, ApiResult> {
    protected final GoogleMap map;
    protected final Map<Marker,ArcadeLocation> markers;
    protected final ProgressBarController pbc;
    protected final MessageDisplay display;
    protected final List<LatLngBounds> areas;
    protected final SharedPreferences sharedPref;

    public MapLoader(GoogleMap map, Map<Marker, ArcadeLocation> markers,
                     ProgressBarController pbc, MessageDisplay display,
                     List<LatLngBounds> areas, SharedPreferences sharedPref) {
        super();
        this.map = map;
        this.markers = markers;
        this.pbc = pbc;
        this.display = display;
        this.areas = areas;
        this.sharedPref = sharedPref;

        // Show indeterminate progress bar
        // Assumes this class is constructed followed by a call to execute()
        // where the bar is hidden on data load completion
        pbc.showProgressBar();
    }


    @Override
    protected void onPostExecute(ApiResult result) {
        super.onPostExecute(result);
        pbc.hideProgressBar();

        switch(result.getErrorCode()) {
            case ApiResult.ERROR_OK:
                fillMap(map, markers, result.getLocations());
                areas.add(result.getBounds());
                break;
            case ApiResult.ERROR_OVERSIZED_BOX:
                display.showMessage(R.string.error_zoom);
                break;
            default:
                display.showMessage(R.string.error_api);
        }
    }

    public static void fillMap(GoogleMap map,
                               Map<Marker,ArcadeLocation> markers, List<ArcadeLocation> feed){
        for (ArcadeLocation loc : feed)
        {
            addMarker(map, markers, loc);
        }
    }

    public static void addMarker(GoogleMap map,
                                 Map<Marker,ArcadeLocation> markers, ArcadeLocation loc) {
        float hue = BitmapDescriptorFactory.HUE_RED;

        // Has the location been tagged as closed?
        if (loc.isClosed()) {
            hue = BitmapDescriptorFactory.HUE_ORANGE;
        }
        // Does the location have a DDR machine?
        else if (loc.hasDDR()) {
            hue = BitmapDescriptorFactory.HUE_AZURE;
        }

        markers.put(
                map.addMarker(
                        new MarkerOptions()
                                .position(loc.getLocation())
                                .title(loc.getName())
                                .snippet(loc.getCity())
                                .icon(BitmapDescriptorFactory.defaultMarker(hue))),
                loc);
    }
}
