/*
 * Copyright (C) 2015. Jared Rummler <me@jaredrummler.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jaredrummler.android.devices;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jared on 9/26/2015.
 */
public class CyanogenModScraper {

  private static final String URL = "http://download.cyanogenmod.org/";

  public static List<String> getCodenames() throws IOException {
    Document document = Jsoup.connect(URL).get();
    Elements deviceElements = document.select("a.device");
    List<String> codenames = new ArrayList<>();
    for (Element element : deviceElements) {
      String onclick = element.attr("onclick");
      String codename = onclick.substring(onclick.indexOf("'") + 1, onclick.lastIndexOf("'"));
      codenames.add(codename);
    }
    return codenames;
  }
}
