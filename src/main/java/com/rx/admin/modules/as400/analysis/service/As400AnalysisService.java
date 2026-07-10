package com.rx.admin.modules.as400.analysis.service;

import com.as400parser.cl.ClParserFacade;
import com.as400parser.common.model.IrDocument;
import com.as400parser.common.parser.ParseOptions;
import com.as400parser.common.serializer.IrJsonSerializer;
import com.as400parser.dds.DdsParserFacade;
import com.as400parser.dspf.DspfParserFacade;
import com.as400parser.prtf.PrtfParserFacade;
import com.as400parser.rpg3.Rpg3ParserFacade;
import com.as400parser.rpgle.RpgleParserFacade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class As400AnalysisService {

    private final ObjectMapper objectMapper;

    public Map<String, Object> analyze(String sourceCode, String sourceType, String fileName) {
        Path tempFile = null;
        try {
            String ext = resolveExtension(sourceType, fileName);
            tempFile = Files.createTempFile("as400-", ext);
            Files.writeString(tempFile, sourceCode, StandardCharsets.UTF_8);

            IrDocument doc = parse(tempFile, sourceType);
            IrJsonSerializer serializer = new IrJsonSerializer();
            String json = serializer.serialize(doc);

            Map<String, Object> result = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            // 提取顶层摘要信息便于前端展示
            Map<String, Object> summary = buildSummary(doc, sourceType);
            result.put("_summary", summary);

            return result;
        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            Map<String, Object> errorInfo = new LinkedHashMap<>();
            errorInfo.put("message", e.getMessage());
            errorInfo.put("type", e.getClass().getSimpleName());
            error.put("_error", errorInfo);
            return error;
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (IOException e) { log.debug("临时文件清理失败", e); }
            }
        }
    }

    private IrDocument parse(Path file, String sourceType) {
        ParseOptions options = ParseOptions.defaults();
        return switch (sourceType.toUpperCase()) {
            case "RPGLE", "SQLRPGLE" -> new RpgleParserFacade().parse(file, options);
            case "RPG3", "RPG", "RPG38" -> new Rpg3ParserFacade().parse(file, options);
            case "CL", "CLP", "CLLE" -> new ClParserFacade().parse(file, options);
            case "DDS_PF", "PF" -> new DdsParserFacade().parse(file, options);
            case "DDS_LF", "LF" -> new DdsParserFacade().parse(file, options);
            case "DSPF" -> new DspfParserFacade().parse(file, options);
            case "PRTF" -> new PrtfParserFacade().parse(file, options);
            default -> throw new IllegalArgumentException("Unsupported source type: " + sourceType);
        };
    }

    private String resolveExtension(String sourceType, String fileName) {
        if (fileName != null && !fileName.isBlank()) {
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0) return fileName.substring(dot);
        }
        return switch (sourceType.toUpperCase()) {
            case "RPGLE", "SQLRPGLE" -> ".rpgle";
            case "RPG3", "RPG" -> ".rpg";
            case "CL", "CLP", "CLLE" -> ".clp";
            case "DDS_PF", "PF" -> ".pf";
            case "DDS_LF", "LF" -> ".lf";
            case "DSPF" -> ".dspf";
            case "PRTF" -> ".prtf";
            default -> ".txt";
        };
    }

    private Map<String, Object> buildSummary(IrDocument doc, String sourceType) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("sourceType", sourceType);

        var metadata = doc.getMetadata();
        if (metadata != null) {
            summary.put("sourceMember", metadata.getSourceMember());
            var parseInfo = metadata.getParseInfo();
            if (parseInfo != null) {
                summary.put("parseStatus", parseInfo.getParseStatus());
                summary.put("totalLines", parseInfo.getTotalLines());
            }
        }

        var deps = doc.getDependencies();
        if (deps != null) {
            summary.put("referencedFiles", deps.getReferencedFiles().stream()
                    .map(ref -> ref.getName() != null ? ref.getName() : ref.toString()).toList());
            summary.put("calledPrograms", deps.getCalledPrograms().stream()
                    .map(ref -> ref.getName() != null ? ref.getName() : ref.toString()).toList());
        }

        return summary;
    }
}
