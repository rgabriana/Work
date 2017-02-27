require 'spec_helper'
describe 'ssh' do

  context 'with defaults for all parameters' do
    it { should contain_class('ssh') }
  end
end
