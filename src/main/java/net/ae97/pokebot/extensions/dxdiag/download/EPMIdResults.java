package net.ae97.pokebot.extensions.dxdiag.download;

import java.util.List;

/**
 * UrielSalads
 * Copyright (C) 2016 Uriel Salischiker
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class EPMIdResults {
    public SearchCriteriaImpl SearchCriteria;
    public List<ResultsForDisplayImpl> ResultsForDisplay;
    public List<TypeFilterImpl> TypeFilter;
    public List<OSFilterImpl> OSFilter;
    public String ProductChilder;
    public String SelectedProduct;
    public String ProductContextualization;
    public int RequestedPage;
    public String CurrentSearchTerm;
    public boolean ProductIsEois;

    public static class SearchCriteriaImpl {
        int NumOfResult;
        String LanguageId;
        String Keyword;
        int HitsPerPage;
        int Offset;
    }

    public static class ResultsForDisplayImpl {
        public int Id;
        public String FullDescriptionUrl;
        public String Title;
        public String SummaryDescription;
        public String OperatingSystems;
        public String OperatingSystemIds;
        public String DownloadType;
        public String PublishDate;
        public String PublishDateMMDDYYYY;
        public String Version;
        public String VersionStatus;
        public List<String> OperatingSystemSet;
        public boolean InIduu;
    }

    public static class TypeFilterImpl {
        String Id;
        String Label;
        int Quantity;
    }

    public static class OSFilterImpl {
        String Id;
        String Label;
        int Quantity;
    }
}
