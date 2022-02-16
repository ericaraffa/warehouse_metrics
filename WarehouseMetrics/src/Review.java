import java.util.Date;

public class Review {
    String reviewerID;
    String asin;
    String reviewerName;
    String reviewText;
    double overall;
    int unixRevTime;
    Date reviewTime;

    public Review(String reviewerID, String asin, String reviewerName, String reviewText, double overall, int unixRevTime, Date reviewTime) {
        this.reviewerID = reviewerID;
        this.asin = asin;
        this.reviewerName = reviewerName;
        this.reviewText = reviewText;
        this.overall = overall;
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
        return reviewText;
    }

    public double getOverall() {
        return overall;
    }

    public int getUnixRevTime() {
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
                ", ReviewText='" + reviewText + '\'' +
                ", Overall=" + overall +
                ", unixRevTime='" + unixRevTime + '\'' +
                ", reviewTime=" + reviewTime +
                '}';
    }
}
