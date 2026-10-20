package school.sptech.dto;

public class IssueRequestDto {

    private IssueFieldsDto fields;

    public IssueRequestDto() {
    }

    public IssueRequestDto(IssueFieldsDto fields) {
        this.fields = fields;
    }

    public IssueFieldsDto getFields() {
        return fields;
    }

    public void setFields(IssueFieldsDto fields) {
        this.fields = fields;
    }
}