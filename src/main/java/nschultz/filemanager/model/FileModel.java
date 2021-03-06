/*
Copyright (c) 2017 Niklas Schultz
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.
 */
package nschultz.filemanager.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FileModel {

  private final StringProperty name;
  private final StringProperty type;
  private final StringProperty size;
  private final StringProperty date;
  private final StringProperty absolutePath;

  public FileModel(String name, String type, String size, String date, String absolutePath) {
    this.name = new SimpleStringProperty(name);
    this.type = new SimpleStringProperty(type);
    this.size = new SimpleStringProperty(size);
    this.date = new SimpleStringProperty(date);
    this.absolutePath = new SimpleStringProperty(absolutePath);
  }

  public String getName() {
    return name.get();
  }

  public String getType() {
    return type.get();
  }

  public String getSize() {
    return size.get();
  }

  public String getDate() {
    return date.get();
  }

  public String getAbsolutePath() {
    return absolutePath.get();
  }
}
