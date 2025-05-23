// ProposalFileService.java
package org.unisoftware.gestioncurricular.service.files;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unisoftware.gestioncurricular.config.BucketsConfig;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;
import org.unisoftware.gestioncurricular.dto.files.ProposalFileDTO;
import org.unisoftware.gestioncurricular.entity.Proposal;
import org.unisoftware.gestioncurricular.repository.ProposalRepository;
import org.unisoftware.gestioncurricular.repository.files.StorageObjectRepository;
import org.unisoftware.gestioncurricular.util.PublicFileUrlBuilder;

@Service
@Transactional
public class ProposalFileService {

    private final ProposalRepository proposalRepository;
    private final SupabaseProperties supabaseProperties;
    private final PublicFileUrlBuilder urlBuilder;
    private final StorageObjectRepository storageObjectRepository;

    public ProposalFileService(ProposalRepository proposalRepository,
                               SupabaseProperties supabaseProperties,
                               PublicFileUrlBuilder urlBuilder,
                               StorageObjectRepository storageObjectRepository) {
        this.proposalRepository = proposalRepository;
        this.supabaseProperties = supabaseProperties;
        this.urlBuilder = urlBuilder;
        this.storageObjectRepository = storageObjectRepository;
    }

    public ProposalFileDTO generateUploadUrl(Long proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        if (!proposal.isCanEdit()) {
            throw new IllegalStateException("Proposal cannot be edited");
        }

        String filename = String.format("propuesta_%d.pdf", proposalId);
        String path = String.format("propuestas/%d/%s", proposal.getCourse().getId(), filename);

        String url = String.format("%s/storage/v1/object/public/%s/%s",
                supabaseProperties.getUrl(), BucketsConfig.PUBLIC_BUCKET, path);

        ProposalFileDTO dto = new ProposalFileDTO();
        dto.setId(proposal.getFile() != null ? proposal.getFile().getId() : null);
        dto.setUrl(url);
        return dto;
    }

    public ProposalFileDTO getProposalFileUrl(Long proposalId, Long courseId) {
        Proposal proposal = proposalRepository.findByIdAndCourseId(proposalId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found for given course"));

        if (proposal.getFile() == null) {
            throw new IllegalStateException("Proposal has no associated file");
        }

        return storageObjectRepository.findById(proposal.getFile().getId())
                .map(storageObject -> {
                    ProposalFileDTO dto = new ProposalFileDTO();
                    dto.setId(storageObject.getId());
                    dto.setUrl(urlBuilder.buildUrl(BucketsConfig.PUBLIC_BUCKET, storageObject.getName()));
                    return dto;
                })
                .orElseThrow(() -> new IllegalStateException("File not found in storage"));
    }
}
