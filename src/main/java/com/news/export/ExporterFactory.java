package com.news.export;

import com.news.export.impl.CsvExporter;
import com.news.export.impl.HtmlExporter;
import com.news.export.impl.JsonExporter;
import com.news.model.ExportFormat;

public class ExporterFactory {
    public static Exporter getExporter(ExportFormat format) {
        return switch (format) {
            case CSV -> new CsvExporter();
            case HTML -> new HtmlExporter();
            case JSON -> new JsonExporter();
        };
    }
}
