package geb.mobile

import spock.lang.Unroll

class AbstractMobileNonEmptyNavigatorSpec extends BaseMobileNonEmptyNavigatorSpec<TestMobileNonEmptyNavigator> {
    @Override
    TestMobileNonEmptyNavigator createNavigator() {
        new TestMobileNonEmptyNavigator(mockBrowser, [mockContextElement])
    }

    @Unroll
    def 'text - with text: [#expectedText]'() {
        when:
        String actualText = navigator.text()

        then:
        interaction { setupDefaultMocking() }
        expectedText == actualText
        1 * mockContextElement.text >> expectedText
        0 * _

        where:
        expectedText << ['test', '']
    }
}
