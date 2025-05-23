package org.unisoftware.gestioncurricular.util;

import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.config.BucketsConfig;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;

@Component
public class PublicFileUrlBuilder {

    private final SupabaseProperties supabaseProperties;

    public PublicFileUrlBuilder(SupabaseProperties supabaseProperties) {
        this.supabaseProperties = supabaseProperties;
    }

    public String buildUploadUrl(String bucket, String path) {
        return String.format("%s/storage/v1/object/%s/%s",
                supabaseProperties.getUrl(), bucket, path);
    }

    public String buildUrl(String bucket, String path) {
        return String.format("%s/storage/v1/object/public/%s/%s",
                supabaseProperties.getUrl(), bucket, path);
    }

    public String buildProgramCurriculumUrl(Long programId, String filename) {
        String path = String.format("programas/%d/curriculums/%s", programId, filename);
        return buildUrl(BucketsConfig.PUBLIC_BUCKET, path);
    }

    public String buildProgramResultadosUrl(Long programId, String filename) {
        String path = String.format("programas/%d/resultados/%s", programId, filename);
        return buildUrl(BucketsConfig.PUBLIC_BUCKET, path);
    }

    public String buildCourseMicrocurriculumUrl(Long courseId, String filename) {
        String path = String.format("cursos/%d/microcurriculums/%s", courseId, filename);
        return buildUrl(BucketsConfig.PUBLIC_BUCKET, path);
    }

    public String buildApoyoUrl(Long courseId, String filename) {
        String path = String.format("cursos/%d/apoyos/%s", courseId, filename);
        return buildUrl(BucketsConfig.PUBLIC_BUCKET, path);
    }

    public String buildProposalUrl(Long proposalId, String filename) {
        String path = String.format("propuestas/%d/%s", proposalId, filename);
        return buildUrl(BucketsConfig.PUBLIC_BUCKET, path);
    }
}
