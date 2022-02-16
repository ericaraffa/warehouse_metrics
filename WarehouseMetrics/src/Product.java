public class Product {

    String metadataid;
    String asin;
    String categories;
    String title;
    double price;

    public Product(String metadataid, String asin, String categories, String title, double price) {
        this.metadataid = metadataid;
        this.asin = asin;
        this.categories = categories;
        this.title = title;
        this.price = price;
    }

    public String getMetadataid() {
        return metadataid;
    }

    public String getAsin() {
        return asin;
    }

    public String getCategories() {
        return categories;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "metadataid='" + metadataid + '\'' +
                ", asing='" + asin + '\'' +
                ", Category='" + categories + '\'' +
                ", Title='" + title + '\'' +
                ", Price=" + price +
                '}';
    }
}
