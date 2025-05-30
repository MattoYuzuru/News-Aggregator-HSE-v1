package com.news.executor.impl.system.export;

import com.news.executor.impl.system.export.impl.CsvExporter;
import com.news.executor.impl.system.export.impl.HtmlExporter;
import com.news.executor.impl.system.export.impl.JsonExporter;
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
