package com.sellsphere.client.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleType;
import com.sellsphere.common.entity.FooterSection;
import com.sellsphere.common.entity.NavigationItem;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ArticleFilter implements Filter {

    private final FooterSectionRepository footerSectionRepository;
    private final NavigationItemRepository navigationItemRepository;
    private final ArticleRepository articleRepository;

    private static final String[] RESOURCE_EXTENSION = {".css", ".js", ".png"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        boolean allowedUrl = isRequestForResource(servletRequest);

        if (!allowedUrl) {
            loadFooterSections(servletRequest);
            loadNavigationItems(servletRequest);
            loadFreeArticles(servletRequest);
        }

        chain.doFilter(request, response);
    }

    private void loadFooterSections(ServletRequest request) {
        List<FooterSection> footerSectionList =
                footerSectionRepository.findAllByOrderBySectionNumberAsc();
        request.setAttribute("footerSectionList", footerSectionList);
    }

    private void loadNavigationItems(HttpServletRequest request) {
        List<NavigationItem> navigationItemList =
                navigationItemRepository.findByArticle_PublishedTrueOrderByItemNumberAsc();
        request.setAttribute("navigationItemList", navigationItemList);
    }

    private void loadFreeArticles(HttpServletRequest request) {
        List<Article> freeArticleList =
                articleRepository.findAllByArticleTypeAndPublishedIsTrue(
                ArticleType.FREE);
        request.setAttribute("freeArticleList", freeArticleList);
    }

    private boolean isRequestForResource(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        for (String allowedExtension : RESOURCE_EXTENSION) {
            if (url.endsWith(allowedExtension)) {
                return true;
            }
        }
        return false;
    }
}
