package net.bteuk.network.utils.plotsystem;

import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to construct a feedback book based on a plot review.
 */
public final class ReviewFeedback {

    private static final Component REVIEW_BOOK_TITLE = ChatUtils.title("Review Book");

    private ReviewFeedback() {
        // Private constructor.
    }

    /**
     * Create the feedback book for a plot review.
     *
     * @param reviewId the id of the plot review
     * @return the feedback book for the plot review
     */
    public static Book createFeedbackBook(int reviewId) {

        Component firstPage = Component.empty();

        // Title
        firstPage = firstPage.append(Component.text("Feedback").decorate(TextDecoration.UNDERLINED).decorate(TextDecoration.BOLD).appendNewline().appendNewline());

        // Reviewer
        String reviewer = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" +
                Network.getInstance().getPlotSQL().getString("SELECT reviewer FROM plot_review WHERE id=" + reviewId + ";")
                + "';");
        firstPage = firstPage.append(Component.text(String.format("Reviewer: %s", reviewer), NamedTextColor.DARK_GRAY)).appendNewline();

        List<Component> pages = new ArrayList<>();

        // Add each category that has a selection.
        Map<ReviewCategory, ReviewCategoryFeedback> reviewCategoryFeedback = getReviewCategoryFeedback(reviewId);
        for (ReviewCategory category : ReviewCategory.values()) {
            ReviewCategoryFeedback categoryFeedback = reviewCategoryFeedback.get(category);
            if (categoryFeedback == null) {
                continue;
            }
            firstPage = firstPage.appendNewline();
            // Add the category to the book.
            firstPage = firstPage.append(addCategoryToFeedbackBook(categoryFeedback, pages));
        }

        // Insert the first page of the book at the start.
        pages.addFirst(firstPage);

        return Book.book(REVIEW_BOOK_TITLE, ChatUtils.line(reviewer), pages);
    }

    private static Map<ReviewCategory, ReviewCategoryFeedback> getReviewCategoryFeedback(int reviewId) {

        Map<ReviewCategory, ReviewCategoryFeedback> reviewCategoryFeedbackMap = new HashMap<>();

        // Get the feedback for the review.
        List<String> reviewCategories = Network.getInstance().getPlotSQL().getStringList("SELECT category FROM plot_category_feedback WHERE review_id=" + reviewId + ";");
        for (String category : reviewCategories) {
            reviewCategoryFeedbackMap.put(ReviewCategory.valueOf(category), new ReviewCategoryFeedback(
                    ReviewCategory.valueOf(category),
                    ReviewSelection.valueOf(Network.getInstance().getPlotSQL().getString("SELECT selection FROM plot_category_feedback WHERE review_id=" + reviewId + " AND category='" + category + "';")),
                    Network.getInstance().getPlotSQL().getInt("SELECT book_id FROM plot_category_feedback WHERE review_id=" + reviewId + " AND category='" + category + "';")
            ));
        }

        return reviewCategoryFeedbackMap;
    }

    @NotNull
    private static Component addCategoryToFeedbackBook(ReviewCategoryFeedback categoryFeedback, List<Component> pages) {

        Component line = Component.empty();
        Component category = Component.text(categoryFeedback.category().getDisplayName(), Style.style(TextDecoration.BOLD));

        // Add the feedback to the book if it exists.
        if (categoryFeedback.bookId() != 0) {
            ArrayList<String> sPages = Network.getInstance().getPlotSQL().getStringList("SELECT contents FROM book_data WHERE id=" + categoryFeedback.bookId() + " ORDER BY page ASC;");

            category = category.clickEvent(getGotoFeedbackClickEvent(pages.size() + 2))
                    .hoverEvent(HoverEvent.showText(Component.text(String.format("Click to go view %s feedback.", categoryFeedback.category().getDisplayName()))));
            category = category.color(NamedTextColor.DARK_BLUE); // Blue colour to indicate that you can navigate to the feedback.
            pages.addAll(sPages.stream().map(Component::text).toList());

            if (categoryFeedback.selection() != ReviewSelection.NONE) {
                category = category.append(Component.text(":"));
            }
        }
        line = line.append(category);

        // Selection
        line = line.appendSpace();
        line = line.append(categoryFeedback.selection().getDisplayComponent());

        return line;
    }

    private static ClickEvent getGotoFeedbackClickEvent(int page) {
        return ClickEvent.clickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(page));
    }
}
