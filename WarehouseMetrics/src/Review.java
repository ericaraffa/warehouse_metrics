import java.util.Date;

public class Review {
    String reviewerID;
    String asin;
    String reviewerName;
    String ReviewText;
    Float Overall;
    String unixRevTime;
    Date reviewTime;

    public Review(String reviewerID, String asin, String reviewerName, String reviewText, Float overall, String unixRevTime, Date reviewTime) {
        this.reviewerID = reviewerID;
        this.asin = asin;
        this.reviewerName = reviewerName;
        ReviewText = reviewText;
        Overall = overall;
        this.unixRevTime = unixRevTime;
        this.reviewTime = reviewTime;
    }

    public String getReviewerID() {
        return reviewerID;
    }

    public String getAsin() {
        return asin;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewText() {
        return ReviewText;
    }

    public Float getOverall() {
        return Overall;
    }

    public String getUnixRevTime() {
        return unixRevTime;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewerID='" + reviewerID + '\'' +
                ", asin='" + asin + '\'' +
                ", reviewerName='" + reviewerName + '\'' +
                ", ReviewText='" + ReviewText + '\'' +
                ", Overall=" + Overall +
                ", unixRevTime='" + unixRevTime + '\'' +
                ", reviewTime=" + reviewTime +
                '}';
    }
}
