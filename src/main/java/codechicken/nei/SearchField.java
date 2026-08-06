package codechicken.nei;

import codechicken.nei.api.ItemFilter;

import java.util.ArrayList;
import java.util.List;

public class SearchField {
    public static List<ISearchProvider> searchProviders = new ArrayList<>();

    public static String getSearchExpression() {
        return "";
    }

    public interface ISearchProvider {
        ItemFilter getFilter(String searchText);

        boolean isPrimary();
    }
}
