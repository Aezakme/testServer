package httpserver;

/**
 * Created by Ghost on 29.08.2017.
 */
public enum ContentType {
    CSS,
    GIF,
    HTM,
    HTML,
    ICO,
    JPG,
    JPEG,
    PNG,
    TXT,
    JS,
    XML;

    @Override
    public String toString() {
        switch (this) {
            case CSS:
                return "Content-Type: text/css";
            case GIF:
                return "Content-Type: image/gif";
            case HTM:
            case HTML:
                return "Content-Type: text/html";
            case ICO:
                return "Content-Type: image/gif";
            case JPG:
            case JPEG:
                return "Content-Type: image/jpeg";
            case PNG:
                return "Content-Type: image/png";
            case TXT:
                return "Content-type: text/plain";
            case XML:
                return "Content-type: text/xml";
            case JS:
                return "Content-Type: text/jscript";
            default:
                return null;
        }
    }
}