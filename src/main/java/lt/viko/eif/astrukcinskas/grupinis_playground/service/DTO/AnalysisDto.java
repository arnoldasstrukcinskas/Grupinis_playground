package lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lt.viko.eif.astrukcinskas.grupinis_playground.model.Analysis;

@JsonPropertyOrder({
        "id",
        "analysis"
})
public class AnalysisDto {

    private int id;
    private String analysis;

    public AnalysisDto(Analysis analysis) {
        this.id = analysis.getId();
        this.analysis = analysis.getAnalysis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
}
