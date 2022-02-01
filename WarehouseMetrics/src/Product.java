public class Product {

    String metadataid;
    String asing;
    String Category;
    String Title;
    Float Price;

    public Product(String metadataid, String asing, String category, String title, Float price) {
        this.metadataid = metadataid;
        this.asing = asing;
        Category = category;
        Title = title;
        Price = price;
    }

    public String getMetadataid() {
        return metadataid;
    }

    public String getAsing() {
        return asing;
    }

    public String getCategory() {
        return Category;
    }

    public String getTitle() {
        return Title;
    }

    public Float getPrice() {
        return Price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "metadataid='" + metadataid + '\'' +
                ", asing='" + asing + '\'' +
                ", Category='" + Category + '\'' +
                ", Title='" + Title + '\'' +
                ", Price=" + Price +
                '}';
    }
}
