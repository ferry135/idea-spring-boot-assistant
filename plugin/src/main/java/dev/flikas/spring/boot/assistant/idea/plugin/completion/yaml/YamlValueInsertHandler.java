package dev.flikas.spring.boot.assistant.idea.plugin.completion.yaml;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.util.text.CharArrayUtil;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

// a large section of this class is borrowed from https://github.com/zalando/intellij-swagger
@ThreadSafe
class YamlValueInsertHandler implements InsertHandler<LookupElement> {
  public static final YamlValueInsertHandler INSTANCE = new YamlValueInsertHandler();

  private static final char SINGLE_QUOTE = '\'';
  private static final char DOUBLE_QUOTE = '"';

  private static final char[] RESERVED_YAML_CHARS =
      {':', '{', '}', '[', ']', ',', '&', '*', '#', '?', '|', '-', '<', '>', '=', '!', '%', '@',
          '`'};


  private YamlValueInsertHandler() {}


  public static String unescapeValue(String value) {
    return value.replaceAll("^['\"]", "").replaceAll("['\"]$", "");
  }


  @Override
  public void handleInsert(@NotNull InsertionContext insertionContext, @NotNull LookupElement lookupElement) {
    if (shouldUseQuotes(lookupElement)) {
      final boolean hasDoubleQuotes =
          hasStartingOrEndingQuoteOfType(insertionContext, lookupElement, DOUBLE_QUOTE);

      if (hasDoubleQuotes) {
        handleEndingQuote(insertionContext, DOUBLE_QUOTE);
        handleStartingQuote(insertionContext, lookupElement, DOUBLE_QUOTE);
      } else {
        handleEndingQuote(insertionContext, SINGLE_QUOTE);
        handleStartingQuote(insertionContext, lookupElement, SINGLE_QUOTE);
      }
    }
  }


  private boolean shouldUseQuotes(final LookupElement lookupElement) {
    return StringUtils.containsAny(lookupElement.getLookupString(), RESERVED_YAML_CHARS);
  }


  private boolean hasStartingOrEndingQuoteOfType(
      final InsertionContext insertionContext,
      final LookupElement lookupElement, final char quoteType
  ) {
    final int caretOffset = insertionContext.getEditor().getCaretModel().getOffset();
    final int startOfLookupStringOffset = caretOffset - lookupElement.getLookupString().length();


    final boolean hasStartingQuote =
        hasStartingQuote(insertionContext, quoteType, startOfLookupStringOffset);
    final boolean hasEndingQuote = hasEndingQuote(insertionContext, caretOffset, quoteType);

    return hasStartingQuote || hasEndingQuote;
  }


  private boolean hasEndingQuote(
      final InsertionContext insertionContext, final int caretOffset,
      final char quoteType
  ) {
    final CharSequence chars = insertionContext.getDocument().getCharsSequence();

    return CharArrayUtil.regionMatches(chars, caretOffset, String.valueOf(quoteType));
  }


  private boolean hasStartingQuote(
      final InsertionContext insertionContext, final char quoteType,
      final int startOfLookupStringOffset
  ) {
    return insertionContext.getDocument().getText().charAt(startOfLookupStringOffset - 1)
        == quoteType;
  }


  private void handleStartingQuote(
      final InsertionContext insertionContext,
      final LookupElement lookupElement, final char quoteType
  ) {
    final int caretOffset = insertionContext.getEditor().getCaretModel().getOffset();
    final int startOfLookupStringOffset = caretOffset - lookupElement.getLookupString().length();

    final boolean hasStartingQuote =
        hasStartingQuote(insertionContext, quoteType, startOfLookupStringOffset);

    if (!hasStartingQuote) {
      insertionContext.getDocument()
          .insertString(startOfLookupStringOffset, String.valueOf(quoteType));
    }
  }


  private void handleEndingQuote(final InsertionContext insertionContext, final char quoteType) {
    final int caretOffset = insertionContext.getEditor().getCaretModel().getOffset();

    final boolean hasEndingQuote = hasEndingQuote(insertionContext, caretOffset, quoteType);

    if (!hasEndingQuote) {
      insertionContext.getDocument().insertString(caretOffset, String.valueOf(quoteType));
    }
  }
}
