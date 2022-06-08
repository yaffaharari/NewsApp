
package com.github.yaffaharari.newsapp.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Article implements Serializable {

    @SerializedName("source")
    @Expose
    private ArticleSource source;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("urlToImage")
    @Expose
    private String urlToImage; //image url
    @SerializedName("url")
    @Expose
    private String url; //link to article
    @SerializedName("publishedAt")
    @Expose
    private String publishedAt;
    @SerializedName("content")
    @Expose
    private String content;

    public Article(ArticleSource source, String author, String title, String description, String urlToImage, String url, String publishedAt, String content) {
        this.source = source;
        this.author = author;
        this.title = title;
        this.description = description;
        this.urlToImage = urlToImage;
        this.url = url;
        this.publishedAt = publishedAt;
        this.content = content;
    }

    public ArticleSource getSource() {
        return source;
    }

    public void setSource(ArticleSource source) {
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(final Object object) {

        if (object instanceof Article) {

            Article target = (Article) object;

            return target.title.equalsIgnoreCase(this.title);

        }

        return false;

    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}